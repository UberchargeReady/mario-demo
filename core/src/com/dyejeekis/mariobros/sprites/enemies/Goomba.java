package com.dyejeekis.mariobros.sprites.enemies;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.Array;
import com.dyejeekis.mariobros.MarioBros;
import com.dyejeekis.mariobros.screens.PlayScreen;
import com.dyejeekis.mariobros.sprites.Mario;

/**
 * Created by George on 6/11/2016.
 */
public class Goomba extends com.dyejeekis.mariobros.sprites.enemies.Enemy {

    private float stateTime;
    private Animation walkAnimation;
    private Array<TextureRegion> frames;

    private boolean setToDestroy;
    private boolean destroyed;

    public Goomba(PlayScreen screen, float x, float y) {
        super(screen, x, y);
        frames = new Array<TextureRegion>();
        for(int i=0; i<2; i++) {
            frames.add(new TextureRegion(screen.getAtlas().findRegion("goomba"), i * 16, 0, 16, 16));
        }
        walkAnimation = new Animation(0.4f, frames);
        stateTime = 0;
        setToDestroy = false;
        destroyed = false;
        setBounds(getX(), getY(), 16 / MarioBros.PPM, 16 / MarioBros.PPM);
    }

    public void update(float dt) {
        stateTime += dt;
        if(setToDestroy && !destroyed) {
            world.destroyBody(body);
            destroyed = true;
            setRegion(new TextureRegion(screen.getAtlas().findRegion("goomba"), 32, 0, 16, 16));
            stateTime = 0;
        }
        else if(!destroyed) {
            body.setLinearVelocity(velocity);
            setPosition(body.getPosition().x - getWidth() / 2, body.getPosition().y - getHeight() / 2);
            setRegion(walkAnimation.getKeyFrame(stateTime, true));
        }
    }

    @Override
    protected void defineEnemy() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(getX(), getY());
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        body = world.createBody(bodyDef);

        FixtureDef fixtureDef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(6 / MarioBros.PPM);
        fixtureDef.filter.categoryBits = MarioBros.ENEMY_BIT;
        fixtureDef.filter.maskBits = MarioBros.GROUND_BIT | MarioBros.COIN_BIT | MarioBros.BRICK_BIT | MarioBros.ENEMY_BIT | MarioBros.OBJECT_BIT | MarioBros.MARIO_BIT;

        fixtureDef.shape = shape;
        body.createFixture(fixtureDef).setUserData(this);

        //create the head here
        PolygonShape head = new PolygonShape();
        Vector2[] vertice = new Vector2[4];
        vertice[0] = new Vector2(-5, 8).scl(1/MarioBros.PPM);
        vertice[1] = new Vector2(5, 8).scl(1/MarioBros.PPM);
        vertice[2] = new Vector2(-3, 3).scl(1/MarioBros.PPM);
        vertice[3] = new Vector2(3, 3).scl(1/MarioBros.PPM);
        head.set(vertice);

        fixtureDef.shape = head;
        //add bounciness on collision with other bodies
        fixtureDef.restitution = 0.5f;
        fixtureDef.filter.categoryBits = MarioBros.ENEMY_HEAD_BIT;
        body.createFixture(fixtureDef).setUserData(this);
    }

    @Override
    public void draw(Batch batch) {
        if(!destroyed || stateTime < 1) {
            super.draw(batch);
        }
    }

    @Override
    public void hitOnHead(Mario mario) {
        setToDestroy = true;
        MarioBros.assetManager.get("audio/sounds/stomp.wav", Sound.class).play();
    }

    public void onEnemyHit(Enemy enemy) {
        if(enemy instanceof Turtle && ((Turtle) enemy).getCurrentState() == Turtle.State.MOVING_SHELL) {
            setToDestroy = true;
        }
        else {
            reverseVelocity(true, false);
        }
    }
}
