package com.dyejeekis.mariobros.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.dyejeekis.mariobros.MarioBros;
import com.dyejeekis.mariobros.scenes.Hud;
import com.dyejeekis.mariobros.sprites.enemies.Enemy;
import com.dyejeekis.mariobros.sprites.Mario;
import com.dyejeekis.mariobros.sprites.items.Item;
import com.dyejeekis.mariobros.sprites.items.ItemDef;
import com.dyejeekis.mariobros.sprites.items.Mushroom;
import com.dyejeekis.mariobros.tools.B2WorldCreator;
import com.dyejeekis.mariobros.tools.WorldContactListener;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by George on 6/5/2016.
 */
public class PlayScreen implements Screen {

    private MarioBros game;

    private OrthographicCamera gameCam;

    private Viewport gameViewport;

    private Hud hud;

    private Mario player;

    //Tiled map fields
    private TmxMapLoader mapLoader;
    private TiledMap tiledMap;
    private OrthogonalTiledMapRenderer renderer;

    //Box2d fields
    private World world;
    private Box2DDebugRenderer b2dr;
    private B2WorldCreator creator;

    private TextureAtlas atlas;

    private Music music;

    private Array<Item> items;
    private LinkedBlockingQueue<ItemDef> itemsToSpawn;

    public PlayScreen(MarioBros game) {
        atlas = new TextureAtlas("Mario_and_Enemies.pack");

        this.game = game;
        gameCam = new OrthographicCamera();
        gameViewport = new FitViewport(MarioBros.V_WIDTH / MarioBros.PPM, MarioBros.V_HEIGHT / MarioBros.PPM, gameCam);
        hud = new Hud(game.batch);

        mapLoader = new TmxMapLoader();
        tiledMap = mapLoader.load("level1.tmx");
        renderer = new OrthogonalTiledMapRenderer(tiledMap, 1/MarioBros.PPM);

        gameCam.position.set(gameViewport.getWorldWidth() / 2, gameViewport.getWorldHeight() / 2, 0);

        Vector2 gravity = new Vector2(0, -10);
        world = new World(gravity, true);
        b2dr = new Box2DDebugRenderer();

        creator = new B2WorldCreator(this);

        //create Mario in our game world
        player = new Mario(this);

        world.setContactListener(new WorldContactListener());

        music = MarioBros.assetManager.get("audio/music/mario_music.ogg", Music.class);
        music.setLooping(true);
        music.setVolume(MarioBros.MUSIC_VOLUME);
        if(MarioBros.ENABLE_MUSIC) {
            music.play();
        }

        items = new Array<Item>();
        itemsToSpawn = new LinkedBlockingQueue<ItemDef>();
    }

    public void spawnItem(ItemDef iDef) {
        itemsToSpawn.add(iDef);
    }

    public void handleSpawningItems() {
        if(!itemsToSpawn.isEmpty()) {
            ItemDef iDef = itemsToSpawn.poll();
            if(iDef.type == Mushroom.class) {
                items.add(new Mushroom(this, iDef.position.x, iDef.position.y));
            }
        }
    }

    @Override
    public void show() {

    }

    public boolean gameOver() {
        if(player.currentState == Mario.State.DEAD && player.getStateTimer() > 3) {
            return true;
        }
        return false;
    }

    public TextureAtlas getAtlas() {
        return atlas;
    }

    public void handleInput(float dt) {
        if(player.currentState != Mario.State.DEAD) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.UP) && player.getState() != Mario.State.JUMPING) {
                player.setState(Mario.State.JUMPING);
                player.body.applyLinearImpulse(new Vector2(0, 4f), player.body.getWorldCenter(), true);
            }
            if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) && player.body.getLinearVelocity().x <= 2) {
                player.body.applyLinearImpulse(new Vector2(0.1f, 0), player.body.getWorldCenter(), true);
            }
            if (Gdx.input.isKeyPressed(Input.Keys.LEFT) && player.body.getLinearVelocity().x >= -2) {
                player.body.applyLinearImpulse(new Vector2(-0.1f, 0), player.body.getWorldCenter(), true);
            }
        }
    }

    public void update(float dt) {
        //handle user input first
        handleInput(dt);

        handleSpawningItems();

        //here we configure how bodies interact during collisions, takes 1 step in the physics simulation (60 times per second)
        world.step(1/60f, 6, 2);

        //update logic for player
        player.update(dt);

        //update logic for enemies
        for(Enemy enemy : creator.getEnemies()) {
            enemy.update(dt);
            //activate enemies (wake from sleep) when a certain distance from the player is reached
            if(enemy.getX() < player.getX() + 224 / MarioBros.PPM) {
                enemy.body.setActive(true);
            }
        }

        //update logic for items
        for(Item item : items) {
            item.update(dt);
        }

        //update our hud
        hud.update(dt);

        //attach our game cam to our player's x coordinate
        if(player.currentState != Mario.State.DEAD) {
            gameCam.position.x = player.body.getPosition().x;
        }

        //update our game camera with correct coordinates after changes
        gameCam.update();

        //tell our renderer to draw only what our camera can see in our game world
        renderer.setView(gameCam);
    }

    @Override
    public void render(float delta) {
        //separate update logic from render
        update(delta);

        //Clear the game screen with black
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        //render our game map
        renderer.render();

        //render our Box2dDebugLines
        b2dr.render(world, gameCam.combined);

        game.batch.setProjectionMatrix(gameCam.combined);
        game.batch.begin();
        player.draw(game.batch);
        for(Enemy enemy : creator.getEnemies()) {
            enemy.draw(game.batch);
        }
        for(Item item : items) {
            item.draw(game.batch);
        }
        game.batch.end();

        //set our batch to now draw what the Hud camera sees
        game.batch.setProjectionMatrix(hud.stage.getCamera().combined);
        hud.stage.draw();

        if(gameOver()) {
            game.setScreen(new GameOverScreen(game));
            dispose();
        }
    }

    @Override
    public void resize(int width, int height) {
        gameViewport.update(width, height);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        tiledMap.dispose();
        renderer.dispose();
        world.dispose();
        b2dr.dispose();
        hud.dispose();
    }

    public TiledMap getTiledMap() {
        return tiledMap;
    }

    public World getWorld() {
        return world;
    }
}
