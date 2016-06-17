package com.dyejeekis.mariobros.sprites.tile_objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.dyejeekis.mariobros.MarioBros;
import com.dyejeekis.mariobros.scenes.Hud;
import com.dyejeekis.mariobros.screens.PlayScreen;
import com.dyejeekis.mariobros.sprites.Mario;
import com.dyejeekis.mariobros.sprites.items.ItemDef;
import com.dyejeekis.mariobros.sprites.items.Mushroom;

/**
 * Created by George on 6/6/2016.
 */
public class Coin extends InteractiveTileObject {

    private static TiledMapTileSet tileSet;
    private final int BLANK_COIN = 28; //Tiled program start counting at 0 index while libgdx starts counting at 1 index, so it's ID + 1

    public Coin(PlayScreen screen, MapObject object) {
        super(screen, object);
        tileSet = map.getTileSets().getTileSet("tileset_gutter");
        fixture.setUserData(this);
        setCategoryFilter(MarioBros.COIN_BIT);
    }

    @Override
    public void onHeadHit(Mario mario) {
        Gdx.app.log("Coin", "Collision");
        if(getCell().getTile().getId() == BLANK_COIN) {
            MarioBros.assetManager.get("audio/sounds/bump.wav", Sound.class).play();
        }
        else {
            getCell().setTile(tileSet.getTile(BLANK_COIN));

            if(object.getProperties().containsKey("mushroom")) { //'mushroom' is a custom property set on the map object with Tiled
                screen.spawnItem(new ItemDef(new Vector2(body.getPosition().x, body.getPosition().y + 16 / MarioBros.PPM),
                        Mushroom.class));
                MarioBros.assetManager.get("audio/sounds/powerup_spawn.wav", Sound.class).play();
            }
            else {
                Hud.addScore(100);
                MarioBros.assetManager.get("audio/sounds/coin.wav", Sound.class).play();
            }
        }
    }
}
