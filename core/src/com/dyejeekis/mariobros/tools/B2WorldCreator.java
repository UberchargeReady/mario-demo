package com.dyejeekis.mariobros.tools;

import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.dyejeekis.mariobros.MarioBros;
import com.dyejeekis.mariobros.screens.PlayScreen;
import com.dyejeekis.mariobros.sprites.enemies.Enemy;
import com.dyejeekis.mariobros.sprites.enemies.Turtle;
import com.dyejeekis.mariobros.sprites.tile_objects.Brick;
import com.dyejeekis.mariobros.sprites.tile_objects.Coin;
import com.dyejeekis.mariobros.sprites.enemies.Goomba;

/**
 * Created by George on 6/6/2016.
 */
public class B2WorldCreator {

    private Array<Goomba> goombas;
    private Array<Turtle> turtles;

    public B2WorldCreator(PlayScreen screen) {
        World world = screen.getWorld();
        TiledMap tiledMap = screen.getTiledMap();

        BodyDef bodyDef = new BodyDef();
        PolygonShape shape = new PolygonShape();
        FixtureDef fixtureDef = new FixtureDef();
        Body body;

        //create ground bodies/fixtures
        for(MapObject object : tiledMap.getLayers().get(2).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rect = ((RectangleMapObject) object).getRectangle();

            bodyDef.type = BodyDef.BodyType.StaticBody;
            bodyDef.position.set((rect.getX() + rect.getWidth() / 2) / MarioBros.PPM, (rect.getY() + rect.getHeight() / 2) / MarioBros.PPM);

            body = world.createBody(bodyDef);

            shape.setAsBox(rect.getWidth() / 2 / MarioBros.PPM, rect.getHeight() / 2 / MarioBros.PPM);
            fixtureDef.shape = shape;
            body.createFixture(fixtureDef);
        }

        //create pipe bodies/fixtures
        for(MapObject object : tiledMap.getLayers().get(3).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rect = ((RectangleMapObject) object).getRectangle();

            bodyDef.type = BodyDef.BodyType.StaticBody;
            bodyDef.position.set((rect.getX() + rect.getWidth() / 2) / MarioBros.PPM, (rect.getY() + rect.getHeight() / 2) / MarioBros.PPM);

            body = world.createBody(bodyDef);

            shape.setAsBox(rect.getWidth() / 2 / MarioBros.PPM, rect.getHeight() / 2 / MarioBros.PPM);
            fixtureDef.shape = shape;
            fixtureDef.filter.categoryBits = MarioBros.OBJECT_BIT;
            body.createFixture(fixtureDef);
        }

        //create brick bodies/fixtures
        for(MapObject object : tiledMap.getLayers().get(5).getObjects().getByType(RectangleMapObject.class)) {
            //Rectangle rect = ((RectangleMapObject) object).getRectangle();

           new Brick(screen, object);
        }

        //create coin bodies/fixtures
        for(MapObject object : tiledMap.getLayers().get(4).getObjects().getByType(RectangleMapObject.class)) {
            //Rectangle rect = ((RectangleMapObject) object).getRectangle();

            new Coin(screen, object);
        }

        //create all goombas
        goombas = new Array<Goomba>();
        for(MapObject object : tiledMap.getLayers().get(6).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rect = ((RectangleMapObject) object).getRectangle();

            goombas.add(new Goomba(screen, rect.getX() / MarioBros.PPM, rect.getY() / MarioBros.PPM));
        }

        //create all turtles
        turtles = new Array<Turtle>();
        for(MapObject object : tiledMap.getLayers().get(7).getObjects().getByType(RectangleMapObject.class)) {
            Rectangle rect = ((RectangleMapObject) object).getRectangle();

            turtles.add(new Turtle(screen, rect.getX() / MarioBros.PPM, rect.getY() / MarioBros.PPM));
        }
    }

    public Array<Goomba> getGoombas() {
        return goombas;
    }

    public Array<Enemy> getEnemies() {
        Array<Enemy> enemies = new Array<Enemy>();
        enemies.addAll(goombas);
        enemies.addAll(turtles);
        return enemies;
    }

}
