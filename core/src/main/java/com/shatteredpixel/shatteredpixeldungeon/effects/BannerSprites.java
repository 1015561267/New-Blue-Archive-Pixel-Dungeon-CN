/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
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

package com.shatteredpixel.shatteredpixeldungeon.effects;

import com.shatteredpixel.shatteredpixeldungeon.Assets;
import com.watabou.noosa.Image;

public class BannerSprites {

	public enum  Type {
		TITLE_PORT,
		TITLE_GLOW_PORT,
		TITLE_LAND,
		TITLE_GLOW_LAND,
		BOSS_SLAIN,
		GAME_OVER,
		SELECT_YOUR_HERO
	}

	public static Image get( Type type ) {
		Image icon = new Image( Assets.Interfaces.BANNERS );
		switch (type) {
			case TITLE_PORT:
				icon.frame( icon.texture.uvRect( 0, 0, 184, 98 ) );
				break;
			case TITLE_GLOW_PORT:
				icon.frame( icon.texture.uvRect( 184, 0, 368, 98 ) );
				break;
			case TITLE_LAND:
				icon.frame( icon.texture.uvRect( 0, 98, 216, 178) );
				break;
			case TITLE_GLOW_LAND:
				icon.frame( icon.texture.uvRect( 216, 98, 432, 178 ) );
				break;
			case BOSS_SLAIN:
				icon.frame( icon.texture.uvRect( 0, 157+64, 128, 192+64 ) );
				break;
			case GAME_OVER:
				icon.frame( icon.texture.uvRect( 0, 192+64, 128, 227+64 ) );
				break;
			case SELECT_YOUR_HERO:
				icon.frame( icon.texture.uvRect( 0, 227+64, 128, 248+64 ) );
				break;
		}
		return icon;
	}
}
