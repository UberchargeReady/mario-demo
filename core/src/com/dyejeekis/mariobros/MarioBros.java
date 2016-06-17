package com.dyejeekis.mariobros;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.dyejeekis.mariobros.screens.PlayScreen;

public class MarioBros extends Game {

	public static final int V_WIDTH = 400;
	public static final int V_HEIGHT = 208;

	public static final float PPM = 100; //pixels per meter

	public static final boolean ENABLE_MUSIC = false;
	public static final float MUSIC_VOLUME = 0.5f;

	public static final short NOTHING_BIT = 0;
	public static final short GROUND_BIT = 1;
	public static final short MARIO_BIT = 2;
	public static final short BRICK_BIT = 4;
	public static final short COIN_BIT = 8;
	public static final short DESTROYED_BIT = 16;
	public static final short OBJECT_BIT = 32;
	public static final short ENEMY_BIT = 64;
	public static final short ENEMY_HEAD_BIT = 128;
	public static final short ITEM_BIT = 256;
	public static final short MARIO_HEAD_BIT = 512;

	public SpriteBatch batch;

	public static AssetManager assetManager; //WARNING: using AssetManager in a static way can cause issues, especially on android
	
	@Override
	public void create () {
		batch = new SpriteBatch();
		assetManager = new AssetManager();
		assetManager.load("audio/music/mario_music.ogg", Music.class);
		assetManager.load("audio/sounds/coin.wav", Sound.class);
		assetManager.load("audio/sounds/bump.wav", Sound.class);
		assetManager.load("audio/sounds/breakblock.wav", Sound.class);
		assetManager.load("audio/sounds/powerup_spawn.wav", Sound.class);
		assetManager.load("audio/sounds/powerup.wav", Sound.class);
		assetManager.load("audio/sounds/powerdown.wav", Sound.class);
		assetManager.load("audio/sounds/stomp.wav", Sound.class);
		assetManager.load("audio/sounds/mariodie.wav", Sound.class);
		assetManager.finishLoading();

		setScreen(new PlayScreen(this));
	}

	@Override
	public void dispose() {
		super.dispose();
		assetManager.dispose();
		batch.dispose();
	}

	@Override
	public void render () {
		super.render();
	}
}
