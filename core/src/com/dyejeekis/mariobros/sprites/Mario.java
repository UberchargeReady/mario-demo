package com.dyejeekis.mariobros.sprites;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.dyejeekis.mariobros.MarioBros;
import com.dyejeekis.mariobros.screens.PlayScreen;
import com.dyejeekis.mariobros.sprites.enemies.Enemy;
import com.dyejeekis.mariobros.sprites.enemies.Turtle;

/**
 * Created by George on 6/6/2016.
 */
public class Mario extends Sprite {

    public enum State { FALLING, JUMPING, STANDING, RUNNING, GROWING, DEAD}

    public State currentState;
    public State previousState;

    public World world;
    public Body body;
    private TextureRegion marioStand;
    private TextureRegion marioJump;
    private TextureRegion bigMarioStand;
    private TextureRegion bigMarioJump;
    private TextureRegion marioDead;

    private Animation marioRun;
    private Animation bigMarioRun;
    private Animation growMario;
    private float stateTimer;

    private boolean runningRight;
    private boolean marioIsBig;
    private boolean playGrowAnimation;
    private boolean defineBigMario;
    private boolean redefineMario;
    private boolean marioIsDead;

    public Mario(PlayScreen screen) {
        this.world = screen.getWorld();
        currentState = State.STANDING;
        previousState = State.STANDING;
        stateTimer = 0;
        runningRight = true;

        Array<TextureRegion> frames = new Array<TextureRegion>();

        //get run animation frames and add them to marioRun Animation
        for(int i=1; i<4; i++) {
            frames.add(new TextureRegion(screen.getAtlas().findRegion("little_mario"), i * 16, 0, 16, 16));
        }
        marioRun = new Animation(0.1f, frames);

        //clear frames for next animation sequence
        frames.clear();

        for(int i=1; i<4; i++) {
            frames.add(new TextureRegion(screen.getAtlas().findRegion("big_mario"), i * 16, 0, 16, 32));
        }
        bigMarioRun = new Animation(0.1f, frames);

        frames.clear();

        //get grow animation frames and add them to growMario Animation
        frames.add(new TextureRegion(screen.getAtlas().findRegion("big_mario"), 15 * 16, 0, 16, 32));
        frames.add(new TextureRegion(screen.getAtlas().findRegion("big_mario"), 0, 0, 16, 32));
        frames.add(new TextureRegion(screen.getAtlas().findRegion("big_mario"), 15 * 16, 0, 16, 32));
        frames.add(new TextureRegion(screen.getAtlas().findRegion("big_mario"), 0, 0, 16, 32));
        growMario = new Animation(0.2f, frames);

        frames.clear();

        //create texture region for mario standing
        marioStand = new TextureRegion(screen.getAtlas().findRegion("little_mario"), 0, 0, 16, 16);
        bigMarioStand = new TextureRegion(screen.getAtlas().findRegion("big_mario"), 0, 0, 16, 32);

        //create texture region for mario jumping
        marioJump = new TextureRegion(screen.getAtlas().findRegion("little_mario"), 5 * 16, 0, 16, 16);
        bigMarioJump = new TextureRegion(screen.getAtlas().findRegion("big_mario"), 5 * 16, 0, 16, 32);

        //create dead mario texture region
        marioDead = new TextureRegion(screen.getAtlas().findRegion("little_mario"), 6 * 16, 0, 16, 16);

        defineMario();
        setBounds(0, 0, 16 / MarioBros.PPM, 16 / MarioBros.PPM);
        setRegion(marioStand);
    }

    public void update(float dt) {
        //update our sprite to correspond with the position of our Box2d body
        if(marioIsBig) {
            setPosition(body.getPosition().x - getWidth() / 2, body.getPosition().y - getHeight() / 2 - 6 / MarioBros.PPM);
        }
        else {
            setPosition(body.getPosition().x - getWidth() / 2, body.getPosition().y - getHeight() / 2);
        }
        //update sprite with the correct frame depending on mario's current action
        setRegion(getFrame(dt));

        if(defineBigMario) {
            defineBigMario();
        }

        if(redefineMario) {
            redefineMario();
        }
    }

    public TextureRegion getFrame(float dt) {
        currentState = getState();

        TextureRegion region;
        switch (currentState) {
            case DEAD:
                region = marioDead;
                break;
            case GROWING:
                region = growMario.getKeyFrame(stateTimer, false);
                if(growMario.isAnimationFinished(stateTimer)) {
                    playGrowAnimation = false;
                }
                break;
            case JUMPING:
                region = marioIsBig ? bigMarioJump : marioJump;
                break;
            case RUNNING:
                region = marioIsBig ? bigMarioRun.getKeyFrame(stateTimer, true) : marioRun.getKeyFrame(stateTimer, true);
                break;
            default:
                region = marioIsBig ? bigMarioStand : marioStand;
                break;
        }

        if((body.getLinearVelocity().x < 0 || !runningRight) && !region.isFlipX()) {
            region.flip(true, false);
            runningRight = false;
        }
        else if((body.getLinearVelocity().x > 0 || runningRight) && region.isFlipX()) {
            region.flip(true, false);
            runningRight = true;
        }

        stateTimer = (currentState == previousState) ? stateTimer + dt : 0;
        previousState = currentState;

        return region;
    }

    public State getState() {
        if(playGrowAnimation) {
            return State.GROWING;
        }
        else if(marioIsDead) {
            return State.DEAD;
        }
        else if((currentState == State.JUMPING && body.getLinearVelocity().y > 0) || (body.getLinearVelocity().y < 0 && previousState == State.JUMPING)) {
            //System.out.println("JUMP!");
            return State.JUMPING;
        }
        else if(body.getLinearVelocity().y < 0) {
            return State.FALLING;
        }
        else if(body.getLinearVelocity().x != 0) {
            //System.out.println("RUNNING..");
            return State.RUNNING;
        }
        return State.STANDING;
    }

    public void setState(State state) {
        this.currentState = state;
    }

    public void defineMario() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(32 / MarioBros.PPM, 32 / MarioBros.PPM);
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        body = world.createBody(bodyDef);

        FixtureDef fixtureDef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(6 / MarioBros.PPM);
        fixtureDef.filter.categoryBits = MarioBros.MARIO_BIT;
        fixtureDef.filter.maskBits = MarioBros.GROUND_BIT |
                MarioBros.COIN_BIT |
                MarioBros.BRICK_BIT |
                MarioBros.OBJECT_BIT |
                MarioBros.ENEMY_BIT |
                MarioBros.ENEMY_HEAD_BIT |
                MarioBros.ITEM_BIT;

        fixtureDef.shape = shape;
        body.createFixture(fixtureDef).setUserData(this);

        EdgeShape head = new EdgeShape();
        head.set(new Vector2(-2 / MarioBros.PPM, 6 / MarioBros.PPM), new Vector2(2 / MarioBros.PPM, 6 / MarioBros.PPM));
        fixtureDef.shape = head;
        fixtureDef.filter.categoryBits = MarioBros.MARIO_HEAD_BIT;
        fixtureDef.isSensor = true;

        body.createFixture(fixtureDef).setUserData(this);
    }

    public void growMario() {
        playGrowAnimation = true;
        marioIsBig = true;
        defineBigMario = true;
        setBounds(getX(), getY(), getWidth(), getHeight() * 2);
        MarioBros.assetManager.get("audio/sounds/powerup.wav", Sound.class).play();
    }

    public void defineBigMario() {
        Vector2 currentPosition = body.getPosition();
        world.destroyBody(body);

        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(currentPosition.add(0, 10 / MarioBros.PPM));
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        body = world.createBody(bodyDef);

        FixtureDef fixtureDef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(6 / MarioBros.PPM);
        fixtureDef.filter.categoryBits = MarioBros.MARIO_BIT;
        fixtureDef.filter.maskBits = MarioBros.GROUND_BIT |
                MarioBros.COIN_BIT |
                MarioBros.BRICK_BIT |
                MarioBros.OBJECT_BIT |
                MarioBros.ENEMY_BIT |
                MarioBros.ENEMY_HEAD_BIT |
                MarioBros.ITEM_BIT;

        fixtureDef.shape = shape;
        body.createFixture(fixtureDef).setUserData(this);
        shape.setPosition(new Vector2(0, -14 / MarioBros.PPM));
        body.createFixture(fixtureDef).setUserData(this);

        EdgeShape head = new EdgeShape();
        head.set(new Vector2(-2 / MarioBros.PPM, 6 / MarioBros.PPM), new Vector2(2 / MarioBros.PPM, 6 / MarioBros.PPM));
        fixtureDef.shape = head;
        fixtureDef.filter.categoryBits = MarioBros.MARIO_HEAD_BIT;
        fixtureDef.isSensor = true;

        body.createFixture(fixtureDef).setUserData(this);
        defineBigMario = false;
    }

    public void redefineMario() {
        Vector2 position = body.getPosition();
        world.destroyBody(body);

        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(position);
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        body = world.createBody(bodyDef);

        FixtureDef fixtureDef = new FixtureDef();
        CircleShape shape = new CircleShape();
        shape.setRadius(6 / MarioBros.PPM);
        fixtureDef.filter.categoryBits = MarioBros.MARIO_BIT;
        fixtureDef.filter.maskBits = MarioBros.GROUND_BIT |
                MarioBros.COIN_BIT |
                MarioBros.BRICK_BIT |
                MarioBros.OBJECT_BIT |
                MarioBros.ENEMY_BIT |
                MarioBros.ENEMY_HEAD_BIT |
                MarioBros.ITEM_BIT;

        fixtureDef.shape = shape;
        body.createFixture(fixtureDef).setUserData(this);

        EdgeShape head = new EdgeShape();
        head.set(new Vector2(-2 / MarioBros.PPM, 6 / MarioBros.PPM), new Vector2(2 / MarioBros.PPM, 6 / MarioBros.PPM));
        fixtureDef.shape = head;
        fixtureDef.filter.categoryBits = MarioBros.MARIO_HEAD_BIT;
        fixtureDef.isSensor = true;

        body.createFixture(fixtureDef).setUserData(this);

        redefineMario = false;
    }

    public boolean isBig() {
        return marioIsBig;
    }

    public void hit(Enemy enemy) {
        if(enemy instanceof Turtle && ((Turtle) enemy).getCurrentState() == Turtle.State.STANDING_SHELL) {
            int speed = (this.getX() <= enemy.getX()) ? Turtle.KICK_RIGHT_SPEED : Turtle.KICK_LEFT_SPEED;
            ((Turtle) enemy).kick(speed);
        }
        else if(marioIsBig) {
            marioIsBig = false;
            redefineMario = true;
            setBounds(getX(), getY(), getWidth(), getHeight() / 2);
            MarioBros.assetManager.get("audio/sounds/powerdown.wav", Sound.class).play();
        }
        else {
            MarioBros.assetManager.get("audio/music/mario_music.ogg", Music.class).stop();
            MarioBros.assetManager.get("audio/sounds/mariodie.wav", Sound.class).play();
            marioIsDead = true;
            Filter filter = new Filter();
            filter.maskBits = MarioBros.NOTHING_BIT;
            for(Fixture fixture : body.getFixtureList()) {
                fixture.setFilterData(filter);
            }
            body.applyLinearImpulse(new Vector2(0, 4f), body.getWorldCenter(), true);
        }
    }

    public boolean isMarioDead() {
        return marioIsDead;
    }

    public float getStateTimer() {
        return stateTimer;
    }

}
