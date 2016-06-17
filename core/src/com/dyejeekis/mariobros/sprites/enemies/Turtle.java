package com.dyejeekis.mariobros.sprites.enemies;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.utils.Array;
import com.dyejeekis.mariobros.MarioBros;
import com.dyejeekis.mariobros.screens.PlayScreen;
import com.dyejeekis.mariobros.sprites.Mario;

/**
 * Created by George on 6/15/2016.
 */
public class Turtle extends Enemy {

    public enum State {
        WALKING, STANDING_SHELL, MOVING_SHELL, DEAD
    }

    public static final int KICK_LEFT_SPEED = -2;
    public static final int KICK_RIGHT_SPEED = 2;

    public State currentState;
    public State previousState;

    private float stateTime;
    private Animation walkAnimation;
    private Array<TextureRegion> frames;

    private TextureRegion shell;

    private boolean setToDestroy;
    private boolean destroyed;

    private float deadRotationDegrees;


    public Turtle(PlayScreen screen, float x, float y) {
        super(screen, x, y);
        frames = new Array<TextureRegion>();
        frames.add(new TextureRegion(screen.getAtlas().findRegion("turtle"), 0, 0, 16, 24));
        frames.add(new TextureRegion(screen.getAtlas().findRegion("turtle"), 16, 0, 16, 24));
        shell = new TextureRegion(screen.getAtlas().findRegion("turtle"), 4 * 16, 0, 16, 24);
        walkAnimation = new Animation(0.2f, frames);

        currentState = previousState = State.WALKING;

        deadRotationDegrees = 0;

        setBounds(getX(), getY(), 16 / MarioBros.PPM, 24 / MarioBros.PPM);
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
        fixtureDef.restitution = 1f;
        fixtureDef.filter.categoryBits = MarioBros.ENEMY_HEAD_BIT;
        body.createFixture(fixtureDef).setUserData(this);
    }

    private TextureRegion getFrame(float dt) {
        TextureRegion region;

        switch (currentState) {
            case MOVING_SHELL:
            case STANDING_SHELL:
                region = shell;
                break;
            case WALKING:
            default:
                region = walkAnimation.getKeyFrame(stateTime, true);
                break;
        }

        if(velocity.x > 0 && region.isFlipX() == false) {
            region.flip(true, false);
        }
        else if(velocity.x < 0 && region.isFlipX() == true) {
            region.flip(true, false);
        }

        stateTime = (currentState == previousState) ? stateTime + dt : 0;
        //update previous state
        previousState = currentState;

        return region;
    }

    @Override
    public void update(float dt) {
        setRegion(getFrame(dt));
        if(currentState == State.STANDING_SHELL && stateTime > 5) {
            currentState = State.WALKING;
            velocity.x = 1f;
        }

        setPosition(body.getPosition().x - getWidth() / 2, body.getPosition().y - 8 / MarioBros.PPM);

        if(currentState == State.DEAD) {
            deadRotationDegrees += 3;
            rotate(deadRotationDegrees);
            if(stateTime > 5 && !destroyed) {
                world.destroyBody(body);
                destroyed = true;
            }
        }
        else {
            body.setLinearVelocity(velocity);
        }
    }

    @Override
    public void hitOnHead(Mario mario) {
        if(currentState != State.STANDING_SHELL) {
            currentState = State.STANDING_SHELL;
            velocity.x = 0;
        }
        else {
            int speed = (mario.getX() <= this.getX()) ? KICK_RIGHT_SPEED : KICK_LEFT_SPEED;
            kick(speed);
        }
    }

    public void kick(int speed) {
        currentState = State.MOVING_SHELL;
        velocity.x = speed;
    }

    public State getCurrentState() {
        return currentState;
    }

    public void onEnemyHit(Enemy enemy) {
        if(enemy instanceof Turtle) {
            Turtle turtle = (Turtle) enemy;
            if(turtle.currentState == State.MOVING_SHELL && this.currentState != State.MOVING_SHELL) {
                this.killed();
            }
            else if(this.currentState == State.MOVING_SHELL && turtle.currentState == State.WALKING) {
                return;
            }
            else {
                reverseVelocity(true, false);
            }
        }
        else if(currentState != State.MOVING_SHELL) {
            reverseVelocity(true, false);
        }
    }

    @Override
    public void draw(Batch batch) {
        if(!destroyed) {
            super.draw(batch);
        }
    }

    public void killed() {
        currentState = State.DEAD;
        Filter filter = new Filter();
        filter.maskBits = MarioBros.NOTHING_BIT;
        for(Fixture fixture : body.getFixtureList()) {
            fixture.setFilterData(filter);
        }
        body.applyLinearImpulse(new Vector2(0, 5f), body.getWorldCenter(), true);
    }
}
