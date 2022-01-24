package net.fabricmc.example.avatars;

import net.fabricmc.example.math.MathUtils;
import net.fabricmc.example.math.Matrix4;
import net.fabricmc.example.math.Vector3;
import net.fabricmc.example.math.Vector4;
import net.fabricmc.example.rendering.textures.TransformTexture;
import org.lwjgl.BufferUtils;
import org.terasology.jnlua.JavaFunction;
import org.terasology.jnlua.LuaRuntimeException;
import org.terasology.jnlua.LuaState;
import org.terasology.jnlua.LuaType;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FiguaModelPart {

    private String name;
    private int luaIndex;

    /**
     * This array contains all of the FiguaModelParts in
     * the same tree as this one. It's built using the
     * static ArrayList, and when you reset the build tree
     * it's copied to the array for all the parts.
     */
    private FiguaModelPart[] treeElements;
    private static ArrayList<FiguaModelPart> currentElements = new ArrayList<>();

    /**
     * The parent of this model part.
     */
    private FiguaModelPart parent;

    /**
     * Contains all the children of this model part.
     */
    private ArrayList<FiguaModelPart> children;

    /**
     * These indices are used for sending data to the TransformTexture.
     * Minimizes the number of calls to glTexSubImage2d.
     */
    private static int currentTexIndex;
    private int firstChildTexIndex;
    private int lastChildTexIndex;

    /**
     * Whether or not this part has vertex data.
     */
    private final boolean hasVertexData;

    /**
     * There are 2 ways for users to modify the transform.
     * They can directly set the value of the matrix4:
     * - Resets rotation, translation, and scale (not pivot) to default values.
     * They can use commands like setRot(), setPos(), etc.
     * - Set pivot, rotation, translation, or scale.
     * - Sets needsTransformUpdate to true.
     * Doing either will set needsTextureUpdate to true.
     * Every frame, if needsTextureUpdate is true:
     * - If not direct mode, update the transform based on pivot, rot, scale, pos.
     * Upload the transform at my index.
     */
    private boolean needsTextureUpdate;
    private boolean needsTransformUpdate;

    private Matrix4 transform;
    private Vector3 pivot;
    private Vector3 rotation;
    private Vector3 translation;
    private Vector3 scale;

    /**
     * Not sure yet what to do about color.
     * Maybe will add a separate texture for other modifications, aside from just the transforms?
     */
    private Vector4 color;
    //More fields

    /**
     * Constructs a new FiguaModelPart from the provided data.
     * @param pivot The pivot point of the new part, in part space.
     * @param rot The rotation of the new part, in degrees.
     * @param hasVertexData Whether this part has vertex data or not. False for groups, true for cuboids and meshes.
     * @param visible Whether this group is visible or not.
     */
    public FiguaModelPart(String name, Vector3 pivot, Vector3 rot, boolean hasVertexData, boolean visible) {
        this.name = name;
        this.hasVertexData = hasVertexData;

        if (hasVertexData) {
            firstChildTexIndex = currentTexIndex;
            lastChildTexIndex = currentTexIndex;
            currentTexIndex++;
        } else {
            firstChildTexIndex = 999999;
            lastChildTexIndex = -999999;
            children = new ArrayList<>();
        }

        translation = Vector3.ZERO;
        scale = Vector3.ONE;
        color = Vector4.ONE;
        transform = Matrix4.IDENTITY;

        needsTextureUpdate = false;
        needsTransformUpdate = false;

        setRot(rot);
        setPivot(pivot);
        if (!visible)
            setTransform(Matrix4.scale(0, 0, 0));

        luaIndex = currentElements.size();
        currentElements.add(this);
    }

    public void addChild(FiguaModelPart child) {
        child.parent = this;
        children.add(child);
        child.updateParentRange();
    }

    private void updateParentRange() {
        if (parent != null) {
            if (parent.firstChildTexIndex > firstChildTexIndex)
                parent.firstChildTexIndex = firstChildTexIndex;
            if (parent.lastChildTexIndex < lastChildTexIndex)
                parent.lastChildTexIndex = lastChildTexIndex;
            parent.updateParentRange();
        }
    }

    /**
     * Call this after all new FiguaModelParts are constructed, and all desired addChild calls are complete.
     */
    public static void resetBuildTree() {
        currentTexIndex = 0;
        FiguaModelPart[] localArr = currentElements.toArray(new FiguaModelPart[currentElements.size()]);
        for (FiguaModelPart part : currentElements)
            part.treeElements = localArr;
        currentElements = new ArrayList<>();
    }

    /**
     * Recursive method which gets everything ready before rendering.
     * Collects model parts' transformations as it traverses the tree,
     * determines which contiguous blocks of texture to upload to, and
     * uploads them to the transformation texture without too many
     * glSubTexImage2D calls.
     */
    public void recursiveSetupPreRender(Matrix4 parentTransform, boolean parentNeededUpdate, TransformTexture tfTex, FloatBuffer buf) {
        if (needsTransformUpdate) {
            updateTransformTRS();
            needsTransformUpdate = false;
        }
        //parentTransform = parentTransform.multiply(transform);
        parentTransform = transform.multiply(parentTransform);

        boolean collector = needsTextureUpdate && !parentNeededUpdate && (firstChildTexIndex <= lastChildTexIndex);
        if (collector)
            buf = BufferUtils.createFloatBuffer((lastChildTexIndex - firstChildTexIndex + 1) * 16);

        if (hasVertexData && buf != null)
            parentTransform.uploadToBuffer(buf);

        if (children != null)
            for (FiguaModelPart child : children)
                child.recursiveSetupPreRender(parentTransform, needsTextureUpdate || parentNeededUpdate, tfTex, buf);

        if (collector) {
            buf.flip();
            tfTex.uploadData(firstChildTexIndex, lastChildTexIndex, buf);
        }

        needsTextureUpdate = false;
    }

    public int getLastChildTexIndex() {
        return lastChildTexIndex;
    }

    /**
     * Updates transform by setting it to the provided matrix.
     * Resets rotation, translation, and scale to default values.
     *
     * @param newTransform
     */
    public void setTransform(Matrix4 newTransform) {
        rotation = Vector3.ZERO;
        translation = Vector3.ZERO;
        scale = Vector3.ONE;
        transform = newTransform;
        needsTextureUpdate = true;
    }


    /**
     * Updates transform based on current values of pivot, rotation, translation, and scale.
     * Goes in TRS order, that is:
     * - Scales
     * - Rotates
     * - Translates
     */
    private void updateTransformTRS() {
        transform = Matrix4.translate(pivot.scale(-1))
                .multiply(Matrix4.scale(scale))
                .multiply(Matrix4.rotateZ(rotation.z())) //I think it's ZYX? Will confirm
                .multiply(Matrix4.rotateY(rotation.y()))
                .multiply(Matrix4.rotateX(rotation.x()))
                .multiply(Matrix4.translate(translation.add(pivot)));
    }

    /**
     * Sets rotation of this model part.
     * Degrees are passed in, but the values are stored in the part in radians.
     * @param newRot
     */
    public void setRot(Vector3 newRot) {
        rotation = newRot.toRad();
        needsTextureUpdate = true;
        needsTransformUpdate = true;
    }

    /**
     * Sets the pivot of this model part.
     * Argument is in part space, but values are stored in world space.
     * @param newPivot
     */
    public void setPivot(Vector3 newPivot) {
        pivot = MathUtils.partToWorld(newPivot);
        needsTextureUpdate = true;
        needsTransformUpdate = true;
    }

    /*
     *
     *
     *
     * LUA STUFF BELOW!
     *
     *
     *
     */

    private static final String ROOT_MODELPART_KEY = "ROOT_MODELPART_KEY";
    private static final String TEMP_GLOBAL_METATABLE = "MODELPART_TEMP_GLOBAL_METATABLE_LOCATION";
    private static final String RESERVED_INDEX_KEY = "FIGUA_RESERVED_INDEX_KEY";

    /**
     * Creates a table for this entire model part, and sets it to a
     * global variable.
     * Also handles all metatable business.
     */
    public void createAsGlobal(LuaState luaState, String globalName) {
        pushModelPartMetatable(luaState); //Push metatable
        luaState.setGlobal(TEMP_GLOBAL_METATABLE); //Set it as a temporary global variable

        //Push the root model part into the registry, so no one else can access it
        luaState.pushJavaObject(this);
        luaState.setField(luaState.REGISTRYINDEX, ROOT_MODELPART_KEY);

        pushTable(luaState); //Push my table on the stack
        luaState.setGlobal(globalName); //Pop it and make global

        luaState.pushNil(); //Push nil on the stack
        luaState.setGlobal(TEMP_GLOBAL_METATABLE); //Set temporary global var back to nil
    }

    /**
     * Creates a table for this model part and pushes it onto the stack.
     * @param luaState The state to create the part in.
     */
    private void pushTable(LuaState luaState) {
        //Generate a new table, this will be the table for this part.
        luaState.newTable();
        //Iterate over each child
        if (children != null)
            for (FiguaModelPart child : children) {
                //Create that child's table and put it on the stack.
                child.pushTable(luaState);
                //Add that child to our table we just created, using the child's name as the key.
                luaState.setField(-2, child.name);
            }
        //Push the lua index for this part onto the stack
        luaState.pushInteger(luaIndex);
        //And add it to the table under a certain name. Hopefully no one names a model part
        //with this name, or else they won't be able to find it :(
        luaState.setField(-2, RESERVED_INDEX_KEY);

        //Get the metatable and put on top of the stack
        luaState.getGlobal(TEMP_GLOBAL_METATABLE);
        //Set that metatable as the metatable for our table we just created
        luaState.setMetatable(-2);
    }

    private static void pushModelPartMetatable(LuaState luaState) {
        //Create new table to be the metatable
        luaState.newTable(); //"metatable"

        //__newindex behavior: prohibit it
        luaState.pushJavaFunction(state -> {
            throw new LuaRuntimeException("Cannot edit model part tables!");
        });
        luaState.setField(-2, "__newindex");

        //Hide this metatable
        luaState.pushBoolean(false);
        luaState.setField(-2, "__metatable");

        //Implement tostring()
        //TODO: actually implement it well lol
        luaState.pushJavaFunction(state -> {
            luaState.pushString("[Model Part Table]");
            return 1;
        });
        luaState.setField(-2, "__tostring");

        //Create additional table to use as value for __index
        //This table will hold all of our functions for model parts
        luaState.newTable(); //"index table"

        for (Map.Entry<String, JavaFunction> entry : luaFunctions.entrySet()) {
            String functionName = entry.getKey();
            JavaFunction function = entry.getValue();

            //Push our java function onto the stack
            luaState.pushJavaFunction(function);
            //Pop our function off the stack, and put it into "index table"
            luaState.setField(-2, functionName);
        }

        //Pop our "index table" off the stack and put it into "metatable", with key "__index"
        luaState.setField(-2, "__index");
    }

    private static final Map<String, JavaFunction> luaFunctions = new HashMap<>() {{
        put("setRot", luaState -> {
            //Ensure argument 2 is a Vector3
            Vector3 newRot = luaState.checkJavaObject(2, Vector3.class);
            FiguaModelPart toModify = getModelPart(luaState);
            toModify.setRot(newRot);
            return 0;
        });
    }};

    /**
     * Gets the FiguaModelPart which is referenced by a table
     * at position 1 on the stack. Returns the stack in the
     * same configuration as when it started.
     *
     * This is a helper function for many methods in the luaFunctions
     * map, since pretty much all of them kinda need to do this.
     *
     * @param luaState The LuaState to get the model part from.
     * @return The FiguaModelPart the table refers to.
     */
    private static FiguaModelPart getModelPart(LuaState luaState) {
        //Ensure argument 1 is in fact a table
        luaState.checkType(1, LuaType.TABLE);
        //Push key
        luaState.pushString(RESERVED_INDEX_KEY);
        //Pop key, push luaIndex
        luaState.rawGet(1);
        //Get index java side
        int index = (int) luaState.checkInteger(-1);
        //Pop luaIndex
        luaState.pop(1);

        //Get registry-held userdata for root part
        luaState.getField(luaState.REGISTRYINDEX, ROOT_MODELPART_KEY);
        //Save it in local var
        FiguaModelPart rootPart = luaState.checkJavaObject(-1, FiguaModelPart.class);
        //Pop userdata
        luaState.pop(1);

        return rootPart.treeElements[index];
    }

}
