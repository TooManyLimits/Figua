
function lerp(a, b, delta)
    return a + (b - a) * delta
end

local rot = vectors.vec3(0, 0, 0)
local lastRot = vectors.vec3(0, 0, 0)

function myTickFunc()
    lastRot = rot
    rot = rot + vectors.vec3(1, 0, 0)
    collectgarbage()
end
events.tick:register(myTickFunc)

function myRenderFunc(delta)
    model.mark:setRot(lerp(lastRot, rot, delta))
end
events.render:register(myRenderFunc)