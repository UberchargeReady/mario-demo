package com.dyejeekis.mariobros.sprites.enemies;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.dyejeekis.mariobros.screens.PlayScreen;
import com.dyejeekis.mariobros.sprites.Mario;

/**
 * Created by George on 6/11/2016.
 */
public abstract class Enemy extends Sprite {

    protected PlayScreen screen;
    protected World world;
    public Body body;
    public Vector2 velocity;

    public Enemy(PlayScreen screen, float x, float y) {
        this.world = screen.getWorld();
        this.screen = screen;
        setPosition(x, y);
        defineEnemy();
        velocity = new Vector2(-1, 0);
        //set b2body to sleep, doesnt get calculated in the simulation
        body.setActive(false);
    }

    protected abstract void defineEnemy();

    public abstract void update(float dt);

    public abstract void hitOnHead(Mario mario);

    public abstract void onEnemyHit(Enemy enemy);

    public void reverseVelocity(boolean x, boolean y) {
        if(x) {
            velocity.x = -velocity.x;
        }
        if (y) {
            velocity.y = -velocity.y;
        }
    }

}
