/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2026 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.items.remains.nba;

import static com.shatteredpixel.shatteredpixeldungeon.Dungeon.hero;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.SPDSettings;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Invisibility;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.RabbitSquadBuff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroSubClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Talent;
import com.shatteredpixel.shatteredpixeldungeon.effects.CellEmitter;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.BlastParticle;
import com.shatteredpixel.shatteredpixeldungeon.effects.particles.SmokeParticle;
import com.shatteredpixel.shatteredpixeldungeon.items.Heap;
import com.shatteredpixel.shatteredpixeldungeon.items.active.Grenade;
import com.shatteredpixel.shatteredpixeldungeon.items.active.HandGrenade;
import com.shatteredpixel.shatteredpixeldungeon.items.remains.RemainsItem;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.mechanics.Ballistica;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;
import com.watabou.utils.PathFinder;
import com.watabou.utils.Random;

import java.util.ArrayList;

public class MiyakoRemain extends NbaRemainsItem{

	{
		image = ItemSpriteSheet.NBARemains.MIYAKO_REMAIN;
		defaultAction = AC_THROW;
	}

	@Override
	protected void doEffect(Hero hero) {
		//this one is viewed as grenade,so no use of this
		//FIXME however copy code to here feels stupid
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		actions.remove(AC_USE);
		return actions;
	}

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);

		if (action.equals(AC_THROW) ){
			doThrow(hero);
		}
	}

	@Override
	protected void onThrow(int cell) {

		Catalog.countUse(getClass());

		Invisibility.dispel();
		Sample.INSTANCE.play( Assets.Sounds.BLAST );

		ArrayList<Char> affected = new ArrayList<>();

		if (Dungeon.level.heroFOV[cell]) {
			CellEmitter.center(cell).burst(BlastParticle.FACTORY, 30);
		}

		boolean terrainAffected = false;
		for (int n : PathFinder.NEIGHBOURS9) {
			int c = cell + n;
			if (c >= 0 && c < Dungeon.level.length()) {
				if (Dungeon.level.heroFOV[c]) {
					CellEmitter.get(c).burst(SmokeParticle.FACTORY, 4);
				}

				if (Dungeon.level.flamable[c]) {
					Dungeon.level.destroy(c);
					GameScene.updateMap(c);
					terrainAffected = true;
				}

				//destroys items / triggers bombs caught in the blast.
				Heap heap = Dungeon.level.heaps.get(c);
				if (heap != null)
					heap.explode();

				Char ch = Actor.findChar(c);
				if (ch != null) {
					affected.add(ch);
				}
			}
		}

		for (Char ch : affected){

			//if they have already been killed by another bomb
			if(!ch.isAlive()){
				continue;
			}

			int dmg = Random.NormalIntRange(explodeMinDmg(), explodeMaxDmg());

			//those not at the center of the blast take less damage
			if (ch.pos != cell){
				dmg = Math.round(dmg*0.67f);
			}

			dmg -= ch.drRoll();

			if (dmg > 0) {
				ch.damage(dmg, Grenade.class);
			}

			if (ch.isAlive()) {
				//effectsOnChar(ch);
			}

			if (ch == hero && !ch.isAlive()) {
				GLog.n(Messages.get(Grenade.class, "ondeath"));
				Dungeon.fail(this);
			}

			if(SPDSettings.rabbitEnhance() && hero.subClass == HeroSubClass.RABBIT_SQUAD && hero.hasTalent(Talent.MIYAKO_EX1_3) &&!ch.isAlive()){
				if(hero.buff(RabbitSquadBuff.MoeCooldown.class)!=null){
					hero.buff(RabbitSquadBuff.MoeCooldown.class).kill(5f * hero.pointsInTalent(Talent.MIYAKO_EX1_3));
				}else Buff.affect(hero, RabbitSquadBuff.MoeEnhance.class).stack();
			}
		}

		if (terrainAffected) {
			Dungeon.observe();
		}
	}

	public int explodeMinDmg(){

		int dmg =5 + Dungeon.scalingDepth();

		if (Dungeon.hero != null) {
			if (Dungeon.hero.hasTalent(Talent.MIYAKO_T1_1)) dmg += Dungeon.hero.pointsInTalent(Talent.MIYAKO_T1_1)+1;
		}
		return dmg;
	}

	public int explodeMaxDmg(){
		int dmg = 10 + Dungeon.scalingDepth()*2;
		if (Dungeon.hero != null) {
			if (Dungeon.hero.hasTalent(Talent.MIYAKO_T1_1)) dmg += Dungeon.hero.pointsInTalent(Talent.MIYAKO_T1_1)+1;
		}
		return dmg;
	}

	@Override
	public int throwPos(Hero user, int dst) {
		if (Dungeon.level.distance(user.pos, dst) < 4) {
			switch (Dungeon.hero.pointsInTalent(Talent.MIYAKO_T3_1)) {
				case 1:
					return new Ballistica( user.pos, dst, Ballistica.STOP_TARGET | Ballistica.STOP_SOLID ).collisionPos;
				case 2:
					return new Ballistica( user.pos, dst, Ballistica.STOP_TARGET | Ballistica.STOP_SOLID | Ballistica.IGNORE_SOFT_SOLID ).collisionPos;
				case 3:
					return new Ballistica( user.pos, dst, Ballistica.STOP_TARGET ).collisionPos;
				case 0: default:
					return super.throwPos(user, dst);
			}
		} else {
			return super.throwPos(user, dst);
		}
	}
}
