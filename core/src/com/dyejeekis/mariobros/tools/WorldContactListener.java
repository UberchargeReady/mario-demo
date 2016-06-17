package com.dyejeekis.mariobros.tools;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.dyejeekis.mariobros.MarioBros;
import com.dyejeekis.mariobros.sprites.Mario;
import com.dyejeekis.mariobros.sprites.enemies.Enemy;
import com.dyejeekis.mariobros.sprites.items.Item;
import com.dyejeekis.mariobros.sprites.tile_objects.InteractiveTileObject;

/**
 * Created by George on 6/9/2016.
 */
public class WorldContactListener implements ContactListener {
    @Override
    public void beginContact(Contact contact) {
        Fixture fixA = contact.getFixtureA();
        Fixture fixB = contact.getFixtureB();

        //result of ORing the collision bits
        int collisionDefinition = fixA.getFilterData().categoryBits | fixB.getFilterData().categoryBits;

        ////getUserData() returns null sometimes
        //try {
        //    if (fixA.getUserData().equals("head") || fixB.getUserData().equals("head")) {
        //        Fixture head = (fixA.getUserData().equals("head")) ? fixA : fixB;
        //        Fixture object = (head == fixA) ? fixB : fixA;
//
        //        if (object.getUserData() != null && object.getUserData() instanceof InteractiveTileObject) {
        //            ((InteractiveTileObject) object.getUserData()).onHeadHit();
        //        }
        //    }
        //} catch (NullPointerException e) {
        //    //System.out.println("getUserData() returns null");
        //}

        switch (collisionDefinition) {
            //mario collides with enemy head
            case MarioBros.ENEMY_HEAD_BIT | MarioBros.MARIO_BIT:
                //System.out.println("Mario collision with enemy head");
                if(fixA.getFilterData().categoryBits == MarioBros.ENEMY_HEAD_BIT) {
                    ((Enemy) fixA.getUserData()).hitOnHead((Mario) fixB.getUserData());
                }
                else {
                    ((Enemy) fixB.getUserData()).hitOnHead((Mario) fixA.getUserData());
                }
                break;
            //enemy collides with an object
            case MarioBros.ENEMY_BIT | MarioBros.OBJECT_BIT:
                if(fixA.getFilterData().categoryBits == MarioBros.ENEMY_HEAD_BIT) {
                    ((Enemy) fixA.getUserData()).reverseVelocity(true, false);
                }
                else {
                    ((Enemy) fixB.getUserData()).reverseVelocity(true, false);
                }
                break;
            //mario collides with an enemy
            case MarioBros.MARIO_BIT | MarioBros.ENEMY_BIT:
                if (fixA.getFilterData().categoryBits == MarioBros.MARIO_BIT) {
                    ((Mario) fixA.getUserData()).hit((Enemy) fixB.getUserData());
                } else {
                    ((Mario) fixB.getUserData()).hit((Enemy) fixA.getUserData());
                }
                break;
            //two enemies collide with each other
            case MarioBros.ENEMY_BIT:
                ((Enemy) fixA.getUserData()).onEnemyHit((Enemy) fixB.getUserData());
                ((Enemy) fixB.getUserData()).onEnemyHit((Enemy) fixA.getUserData());
                //((Enemy) fixA.getUserData()).reverseVelocity(true, false);
                //((Enemy) fixB.getUserData()).reverseVelocity(true, false);
                break;
            //item collides with object
            case MarioBros.ITEM_BIT | MarioBros.OBJECT_BIT:
                if(fixA.getFilterData().categoryBits == MarioBros.ITEM_BIT) {
                    ((Item) fixA.getUserData()).reverseVelocity(true, false);
                }
                else {
                    ((Item) fixB.getUserData()).reverseVelocity(true, false);
                }
                break;
            //mario collides with item
            case MarioBros.ITEM_BIT | MarioBros.MARIO_BIT:
                if (fixA.getFilterData().categoryBits == MarioBros.ITEM_BIT) {
                    ((Item) fixA.getUserData()).useItem((Mario) fixB.getUserData());
                } else {
                    ((Item) fixB.getUserData()).useItem((Mario) fixA.getUserData());
                }
                break;
            //mario's head collides with brick or coin
            case MarioBros.MARIO_HEAD_BIT | MarioBros.BRICK_BIT:
            case MarioBros.MARIO_HEAD_BIT | MarioBros.COIN_BIT:
                if (fixA.getFilterData().categoryBits == MarioBros.MARIO_HEAD_BIT) {
                    ((InteractiveTileObject) fixB.getUserData()).onHeadHit((Mario) fixA.getUserData());
                } else {
                    ((InteractiveTileObject) fixA.getUserData()).onHeadHit((Mario) fixB.getUserData());
                }
                break;
        }
    }

    @Override
    public void endContact(Contact contact) {

    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }
}
