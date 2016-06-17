package com.dyejeekis.mariobros.sprites.tile_objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.math.Rectangle;
import com.dyejeekis.mariobros.MarioBros;
import com.dyejeekis.mariobros.scenes.Hud;
import com.dyejeekis.mariobros.screens.PlayScreen;
import com.dyejeekis.mariobros.sprites.Mario;

/**
 * Created by George on 6/6/2016.
 */
public class Brick extends InteractiveTileObject {

    public Brick(PlayScreen screen, MapObject object) {
        super(screen, object);
        fixture.setUserData(this);
        setCategoryFilter(MarioBros.BRICK_BIT);
    }

    @Override
    public void onHeadHit(Mario mario) {
        Gdx.app.log("Brick", "Collision");
        if(mario.isBig()) {
            setCategoryFilter(MarioBros.DESTROYED_BIT);
            getCell().setTile(null);
            Hud.addScore(200);
            MarioBros.assetManager.get("audio/sounds/breakblock.wav", Sound.class).play();
        }
        else {
            MarioBros.assetManager.get("audio/sounds/bump.wav", Sound.class).play();
        }
    }
}
