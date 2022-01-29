package net.fabricmc.example.management.cards;

import net.fabricmc.example.management.serializers.LocalAvatarSerializer;
import net.minecraft.nbt.NbtCompound;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class LocalAvatarCard extends AvatarCard<Path> {

    public LocalAvatarCard(Path data) {
        super(data);
    }

    /**
     * Follows the provided path and fills the avatar using the data in that folder.
     */
    @Override
    public void fillCard() {
        CompletableFuture.runAsync(() -> {
            //Convert the local files into NBT
            NbtCompound nbt = new LocalAvatarSerializer().serialize(data);

            //Fill the card metadata from NBT


            //Mark metadata as complete
            metadata.complete();

            //Use superclass method to fill the avatar from NBT
            fillAvatarFromNbt(nbt);

            //Mark the avatar as complete
            avatar.complete();
        });
    }
}
