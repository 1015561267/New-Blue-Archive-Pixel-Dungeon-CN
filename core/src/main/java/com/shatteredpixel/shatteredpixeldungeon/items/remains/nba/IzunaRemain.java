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

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Actor;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.ArtifactRecharge;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Barrier;
import com.shatteredpixel.shatteredpixeldungeon.actors.buffs.Buff;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroAction;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.HeroClass;
import com.shatteredpixel.shatteredpixeldungeon.actors.mobs.Mob;
import com.shatteredpixel.shatteredpixeldungeon.effects.FloatingText;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.KindOfWeapon;
import com.shatteredpixel.shatteredpixeldungeon.items.remains.RemainsItem;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfRecharging;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.quick.AssassinsKunai;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.melee.quick.QuickWeapon;
import com.shatteredpixel.shatteredpixeldungeon.journal.Catalog;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.CellSelector;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.CharSprite;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.watabou.noosa.audio.Sample;

import java.util.ArrayList;

public class IzunaRemain extends NbaRemainsItem {

	{
		image = ItemSpriteSheet.NBARemains.IZUNA_REMAIN;
		defaultAction = AC_SLASH;
	}

	public static final String AC_SLASH =  "SLASH";

	@Override
	protected void doEffect(Hero hero) {
		//this one is viewed as grenade,so no use of this
	}

	@Override
	public ArrayList<String> actions(Hero hero) {
		ArrayList<String> actions = super.actions(hero);
		actions.remove(AC_USE);
		actions.add(AC_SLASH);
		return actions;
	}

	@Override
	public void execute(Hero hero, String action) {
		super.execute(hero, action);

		if (action.equals(AC_SLASH) ){
			usesTargeting = true;
			curUser = hero;
			curItem = this;
			GameScene.selectCell(attacker);
		}
	}

	private CellSelector.Listener attacker = new CellSelector.Listener() {
		@Override
		public void onSelect( Integer target ) {
			if (target != null) {
				Char ch = Actor.findChar(target);
				Hero hero = curUser;
				if (ch != null && ch.alignment == Char.Alignment.ENEMY) {
					if (!Dungeon.level.adjacent( hero.pos, target )) {
						GLog.w(Messages.get(QuickWeapon.class, "cannot_reach"));
					} else if(!((Mob) ch).surprisedBy(hero)){
						GLog.w(Messages.get(QuickWeapon.class, "must_surprise"));
					}
					else{
						Catalog.countUse(curItem.getClass());
						curItem.detach(hero.belongings.backpack);
						ArtifactRecharge.chargeArtifacts(Dungeon.hero, 4f);
						ScrollOfRecharging.charge(hero);
						Sample.INSTANCE.play( Assets.Sounds.CHARGEUP );

						KindOfWeapon herosWeapon = hero.belongings.weapon; //기존에 사용하던 무기를 저장
						hero.belongings.weapon = new AssassinsKunai(); //공격에 사용할 무기를 이 무기로 변경
						hero.busy();
						hero.curAction = new HeroAction.Attack( ch ); //영웅이 대상을 공격함
						Buff.affect(hero, TemporaryQuickWeaponTracker.class).setWeapon(herosWeapon); //공격 후 영웅의 무기를 원래대로 되돌리도록 지연시키는 버프
						hero.next();
					}
				} else {
					GLog.w(Messages.get(QuickWeapon.class, "no_enemy"));
				}
			}
		}
		@Override
		public String prompt() {
			return Messages.get(QuickWeapon.class, "prompt");
		}
	};

	public static class TemporaryQuickWeaponTracker extends QuickWeapon.QuickWeaponTracker {
		{
			actPriority = BLOB_PRIO;//FIXME to be honest I don't know why but it should be acted after hero
		}
	}
}
