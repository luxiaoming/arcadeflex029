/*
This file is part of Arcadeflex.

Arcadeflex is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Arcadeflex is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Arcadeflex.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 *  ported to v0.28
 *  ported to v0.27
 *
 *   using v0.36 romset
 *   kungfub is kungfub2 in v0.36romset
 */
package drivers;

import static arcadeflex.libc.*;
import static mame.commonH.*;
import static mame.cpuintrf.*;
import static mame.driverH.*;
import static mame.mame.*;
import static mame.inptport.*;
import static mame.osdependH.*;
import static vidhrdw.generic.*;
import static sndhrdw.generic.*;
import static vidhrdw.kungfum.*;
import static sndhrdw.kungfum.*;
import static mame.memoryH.*;

public class kungfum {
            static MemoryReadAddress readmem[] =
            {
                     new MemoryReadAddress( 0xe000, 0xefff, MRA_RAM ),
                     new MemoryReadAddress( 0xd000, 0xdfff, MRA_RAM ),         /* Video and Color ram */
                     new MemoryReadAddress( 0x0000, 0x7fff, MRA_ROM ),
                     new MemoryReadAddress( -1 )	/* end of table */
            };

            static MemoryWriteAddress writemem[] =
            {
                    new MemoryWriteAddress( 0xe000, 0xefff, MWA_RAM ),
                    new MemoryWriteAddress( 0xd000, 0xd7ff, videoram_w, videoram, videoram_size ),
                    new MemoryWriteAddress( 0xd800, 0xdfff, colorram_w, colorram ),
                    new MemoryWriteAddress( 0xc020, 0xc0df, MWA_RAM, spriteram, spriteram_size ),
                    new MemoryWriteAddress( 0xa000, 0xa000, MWA_RAM, kungfum_scroll_low ),
                    new MemoryWriteAddress( 0xb000, 0xb000, MWA_RAM, kungfum_scroll_high ),
                    new MemoryWriteAddress( 0x0000, 0x7fff, MWA_ROM ),
                    new MemoryWriteAddress( -1 )	/* end of table */
            };


            static IOReadPort readport[] =
            {
                    new IOReadPort( 0x00, 0x00, input_port_0_r ),   /* coin */
                    new IOReadPort( 0x01, 0x01, input_port_1_r ),   /* player 1 control */
                    new IOReadPort( 0x02, 0x02, input_port_2_r ),   /* player 2 control */
                    new IOReadPort( 0x03, 0x03, input_port_3_r ),   /* DSW 1 */
                    new IOReadPort( 0x04, 0x04, input_port_4_r ),   /* DSW 2 */
                    new IOReadPort( -1 )	/* end of table */
            };

            static IOWritePort writeport[] =
            {
                    new IOWritePort( 0x00, 0x00, kungfum_sh_port0_w ),
                    new IOWritePort( -1 )	/* end of table */
            };


            static InputPort input_ports[] =
            {
                    new InputPort(	/* IN0 */
                            0xff,
                           new int[] { OSD_KEY_1, OSD_KEY_2, OSD_KEY_3, 0, 0, 0, 0, 0 }

                    ),
                    new InputPort(	/* IN1 */
                            0xff,
                            new int[]{ OSD_KEY_RIGHT, OSD_KEY_LEFT, OSD_KEY_DOWN, OSD_KEY_UP,
                                            0, OSD_KEY_CONTROL, 0, OSD_KEY_ALT }

                    ),
                    new InputPort(	/* IN2 */
                            0xff,
                           new int[] { 0, 0, 0, 0, 0, 0, 0, 0 }

                    ),
                    new InputPort(	/* DSW1 */
                            0xff,
                           new int[] { 0, 0, 0, 0, 0, 0, 0, 0 }

                    ),
                    new InputPort(	/* DSW2 */
                            0xfd,
                           new int[] { 0, 0, 0, 0, 0, 0, 0, OSD_KEY_F1 }

                    ),
                   new InputPort( -1 )	/* end of table */
            };

            static TrakPort trak_ports[] =
            {
                    new TrakPort(-1)
            };


             static KEYSet[] keys =
            {
                    new KEYSet( 1, 3, "JUMP" ),
                    new KEYSet( 1, 1, "MOVE LEFT"  ),
                    new KEYSet( 1, 0, "MOVE RIGHT" ),
                    new KEYSet( 1, 2, "CROUCH" ),
                    new KEYSet( 1, 5, "PUNCH" ),
                    new KEYSet( 1, 7, "KICK" ),
                    new KEYSet( -1 )
            };


            static DSW dsw[] =
            {
                    new DSW( 3, 0x0c, "LIVES", new String[]{ "5", "4", "2", "3" }, 1 ),
                    new DSW( 3, 0x01, "DIFFICULTY", new String[]{ "HARD", "EASY" }, 1 ),
                    new DSW( 3, 0x02, "TIMER", new String[]{ "FAST", "SLOW" }, 1 ),
                    new DSW( 4, 0x40, "DEMO MODE",new String[] { "ON", "OFF" }, 1 ),
                    new DSW( 4, 0x20, "LEVEL SELECT", new String[]{ "ON", "OFF" }, 1 ),
                    new DSW( 4, 0x08, "SW4B",new String[] { "ON", "OFF" }, 1 ),
                    new DSW( 4, 0x10, "SW5B", new String[]{ "ON", "OFF" }, 1 ),
                    new DSW( -1 )
            };



            static GfxLayout charlayout = new GfxLayout
            (
                    8,8,	/* 8*8 characters */
                    1024,	/* 1024 characters */
                    3,	/* 2 bits per pixel */
                    new int[]{ 0, 0x2000*8, 0x4000*8 },
                    new int[]{ 0, 1, 2, 3, 4, 5, 6, 7 },
                    new int[]{ 8*0, 8*1, 8*2, 8*3, 8*4, 8*5, 8*6, 8*7 },
                    8*8	/* every char takes 8 consecutive bytes */
            );
            static GfxLayout spritelayout = new GfxLayout
            (
                    16,16,	/* 16*16 sprites */
                    256,	/* 256 sprites */
                    3,	/* 3 bits per pixel */
                    new int[]{ 0x10000*8, 0, 0x8000*8 },
                    new int[]{ 0, 1, 2, 3, 4, 5, 6, 7, 16*8+0, 16*8+1, 16*8+2, 16*8+3, 16*8+4, 16*8+5, 16*8+6, 16*8+7},
                    new int[]{ 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8, 8*8, 9*8, 10*8, 11*8, 12*8, 13*8, 14*8, 15*8},
                    32*8	/* every sprite takes 32 consecutive bytes */
            );



            static GfxDecodeInfo gfxdecodeinfo[] =
            {
                    new GfxDecodeInfo( 1, 0x00000, charlayout,           0, 32 ),
                    new GfxDecodeInfo( 1, 0x06000, spritelayout,      32*8, 32 ),
                    new GfxDecodeInfo( 1, 0x08000, spritelayout,      32*8, 32 ),
                    new GfxDecodeInfo( 1, 0x0a000, spritelayout,      32*8, 32 ),
                    new GfxDecodeInfo( 1, 0x0c000, spritelayout,      32*8, 32 ),
                    new GfxDecodeInfo( -1 ) /* end of array */
            };



            static char color_prom[] =
            {
                    /* G-1J - character palette red component */
                    0x00,0x0F,0x00,0x0F,0x00,0x0F,0x00,0x0F,0x0A,0x0E,0x0D,0x0F,0x0E,0x0B,0x0C,0x0E,
                    0x0B,0x0F,0x08,0x0C,0x06,0x0D,0x0B,0x0A,0x00,0x0B,0x0F,0x0A,0x0F,0x0C,0x0A,0x0F,
                    0x0A,0x0C,0x0F,0x0F,0x00,0x0E,0x0B,0x0C,0x0C,0x00,0x0D,0x0D,0x0E,0x0A,0x0C,0x0F,
                    0x0A,0x0D,0x00,0x0D,0x0F,0x0C,0x07,0x0C,0x0E,0x00,0x0F,0x0F,0x00,0x0B,0x0F,0x0F,
                    0x0F,0x0B,0x0F,0x0C,0x0D,0x0F,0x00,0x0D,0x0A,0x0F,0x0C,0x0B,0x0B,0x0E,0x0E,0x0F,
                    0x0B,0x0F,0x0D,0x0E,0x0D,0x00,0x0C,0x07,0x0A,0x0F,0x0F,0x0F,0x0A,0x0C,0x00,0x0F,
                    0x0A,0x00,0x00,0x0F,0x09,0x0C,0x0C,0x0E,0x0F,0x0B,0x0C,0x0E,0x09,0x0A,0x00,0x0B,
                    0x09,0x0B,0x00,0x0F,0x09,0x0C,0x00,0x0B,0x09,0x0F,0x00,0x0F,0x0F,0x0C,0x0D,0x0B,
                    0x09,0x0F,0x0B,0x0E,0x00,0x0D,0x0B,0x0B,0x0A,0x00,0x0C,0x0E,0x0B,0x0F,0x00,0x0B,
                    0x0A,0x00,0x0C,0x0E,0x0C,0x0F,0x0A,0x0B,0x0A,0x0A,0x0C,0x0E,0x0C,0x0F,0x0C,0x0B,
                    0x00,0x0F,0x0F,0x0A,0x0F,0x0F,0x0C,0x0E,0x00,0x00,0x0F,0x0A,0x0F,0x0C,0x0A,0x0B,
                    0x0A,0x09,0x0C,0x0E,0x00,0x0E,0x0B,0x0C,0x0F,0x0F,0x0A,0x0F,0x00,0x0F,0x0F,0x0F,
                    0x0F,0x0F,0x00,0x0F,0x0F,0x0F,0x0F,0x0F,0x0A,0x0F,0x0A,0x0F,0x0F,0x0F,0x00,0x0F,
                    0x0A,0x0F,0x0A,0x0F,0x0F,0x0F,0x00,0x0F,0x0F,0x0A,0x00,0x0F,0x0F,0x0F,0x0D,0x0E,
                    0x06,0x0F,0x0F,0x0D,0x0C,0x08,0x0F,0x0E,0x0A,0x0F,0x0F,0x0E,0x06,0x0E,0x0D,0x0B,
                    0x06,0x0B,0x0F,0x0D,0x00,0x0E,0x0F,0x0F,0x0F,0x0F,0x09,0x0B,0x0A,0x08,0x0D,0x0E,
                    /* B-1M - sprite palette red component */
                    0x00,0x0A,0x0C,0x0F,0x0F,0x0C,0x09,0x0F,0x00,0x0F,0x0C,0x0F,0x0F,0x0A,0x0C,0x0F,
                    0x00,0x0C,0x09,0x0F,0x0F,0x0A,0x00,0x0F,0x00,0x0F,0x0C,0x0F,0x00,0x0A,0x0B,0x0F,
                    0x00,0x09,0x0B,0x0C,0x0D,0x0E,0x0F,0x0F,0x00,0x00,0x0B,0x0C,0x0D,0x0E,0x0F,0x0F,
                    0x00,0x0C,0x0A,0x0E,0x09,0x0B,0x0F,0x0F,0x00,0x0F,0x0F,0x0E,0x0F,0x0E,0x0A,0x0B,
                    0x00,0x0F,0x0F,0x0D,0x0C,0x0A,0x08,0x0B,0x00,0x0E,0x0F,0x0D,0x0C,0x0A,0x08,0x0F,
                    0x00,0x0D,0x07,0x0E,0x00,0x0F,0x07,0x0F,0x00,0x0F,0x08,0x0F,0x07,0x0F,0x0C,0x0F,
                    0x00,0x0F,0x07,0x0F,0x07,0x0F,0x0B,0x0F,0x00,0x0D,0x0F,0x0F,0x00,0x0E,0x08,0x0F,
                    0x00,0x0F,0x0B,0x00,0x08,0x0C,0x0D,0x0F,0x00,0x0F,0x0B,0x0F,0x0F,0x0C,0x0D,0x0F,
                    0x00,0x0F,0x0B,0x0F,0x08,0x0C,0x0D,0x0F,0x00,0x0D,0x09,0x0B,0x00,0x0E,0x0B,0x0F,
                    0x00,0x0D,0x0B,0x0F,0x09,0x0E,0x0D,0x0F,0x00,0x0D,0x0F,0x0E,0x0A,0x0B,0x0B,0x0E,
                    0x00,0x0D,0x08,0x0C,0x0A,0x0B,0x0B,0x0E,0x00,0x0F,0x07,0x0F,0x00,0x0F,0x0C,0x0F,
                    0x00,0x0A,0x0A,0x0D,0x09,0x0F,0x0C,0x0E,0x00,0x0D,0x0A,0x0F,0x09,0x0F,0x0C,0x0E,
                    0x00,0x0F,0x0D,0x0F,0x0B,0x0F,0x0D,0x0E,0x00,0x0F,0x0C,0x0F,0x0B,0x0E,0x0E,0x0D,
                    0x00,0x0F,0x0A,0x0F,0x0B,0x0E,0x0B,0x0D,0x00,0x0F,0x0D,0x0E,0x00,0x0F,0x0A,0x0F,
                    0x00,0x0F,0x0D,0x0E,0x0D,0x0F,0x0C,0x0F,0x00,0x0F,0x0C,0x0D,0x0C,0x0F,0x0F,0x0F,
                    0x00,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x00,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,
                    /* G-1F - character palette green component */
                    0x00,0x00,0x0F,0x0F,0x00,0x00,0x0F,0x0F,0x08,0x0B,0x0A,0x00,0x0E,0x08,0x0A,0x0E,
                    0x0B,0x0F,0x0C,0x0E,0x06,0x09,0x09,0x0A,0x00,0x08,0x0F,0x0C,0x0F,0x09,0x0A,0x0E,
                    0x08,0x03,0x08,0x0B,0x00,0x0E,0x08,0x0A,0x0A,0x0C,0x0F,0x09,0x0E,0x08,0x0C,0x0C,
                    0x08,0x0F,0x0E,0x09,0x0C,0x0C,0x07,0x0A,0x0E,0x0F,0x0C,0x0D,0x0F,0x08,0x0F,0x00,
                    0x00,0x08,0x0F,0x0A,0x09,0x0C,0x0E,0x0F,0x0C,0x0C,0x0A,0x08,0x08,0x0D,0x0E,0x0F,
                    0x08,0x0C,0x09,0x0E,0x0F,0x0E,0x0C,0x07,0x08,0x0F,0x0F,0x00,0x08,0x00,0x00,0x0F,
                    0x08,0x0B,0x00,0x0C,0x09,0x0A,0x0C,0x0E,0x0F,0x08,0x0A,0x0E,0x09,0x0C,0x0D,0x0F,
                    0x09,0x08,0x0E,0x0D,0x09,0x0A,0x0D,0x0F,0x09,0x0B,0x0E,0x0D,0x0F,0x0A,0x0D,0x0B,
                    0x09,0x0B,0x08,0x0E,0x0D,0x0D,0x0F,0x0B,0x08,0x00,0x0A,0x0E,0x08,0x0C,0x0D,0x0A,
                    0x08,0x00,0x0A,0x0E,0x09,0x0C,0x0C,0x08,0x08,0x08,0x0A,0x0E,0x09,0x0C,0x0A,0x08,
                    0x00,0x0F,0x09,0x0A,0x0F,0x0F,0x0C,0x0E,0x00,0x0F,0x00,0x0A,0x0F,0x0C,0x0A,0x0B,
                    0x08,0x09,0x0F,0x0E,0x00,0x0B,0x0E,0x0A,0x0F,0x0F,0x08,0x0F,0x00,0x0F,0x0F,0x0F,
                    0x0F,0x0F,0x00,0x0F,0x00,0x0F,0x0F,0x0F,0x08,0x0F,0x08,0x0F,0x00,0x0F,0x00,0x0F,
                    0x08,0x00,0x08,0x0F,0x00,0x09,0x00,0x0F,0x0E,0x0A,0x00,0x0F,0x00,0x0C,0x0B,0x0C,
                    0x06,0x0F,0x00,0x02,0x0E,0x0C,0x0F,0x0D,0x08,0x0F,0x00,0x0E,0x06,0x0D,0x02,0x08,
                    0x06,0x08,0x0F,0x02,0x0B,0x0D,0x00,0x0F,0x0E,0x00,0x08,0x08,0x0A,0x0C,0x0C,0x0E,
                    /* B-1N - sprite palette green component */
                    0x00,0x07,0x0C,0x0B,0x0C,0x0F,0x09,0x0F,0x00,0x00,0x0C,0x0B,0x0C,0x0D,0x0F,0x0F,
                    0x00,0x0C,0x09,0x0B,0x0C,0x0D,0x00,0x0F,0x00,0x00,0x0F,0x0C,0x00,0x07,0x09,0x0B,
                    0x00,0x09,0x0B,0x0D,0x00,0x0A,0x0B,0x0C,0x00,0x00,0x0B,0x0D,0x00,0x0A,0x0B,0x0F,
                    0x00,0x0D,0x0B,0x0E,0x09,0x08,0x0D,0x0F,0x00,0x0F,0x0F,0x0A,0x0D,0x0C,0x08,0x0F,
                    0x00,0x0B,0x0C,0x0E,0x0D,0x09,0x08,0x0B,0x00,0x0E,0x0C,0x0E,0x0D,0x09,0x08,0x0F,
                    0x00,0x0C,0x0B,0x0D,0x00,0x0F,0x09,0x0F,0x00,0x00,0x0F,0x0B,0x07,0x0F,0x0A,0x0F,
                    0x00,0x00,0x0C,0x0F,0x0A,0x0C,0x0E,0x0F,0x00,0x00,0x0F,0x0F,0x00,0x0A,0x08,0x0C,
                    0x00,0x0C,0x09,0x00,0x08,0x0C,0x0B,0x0F,0x00,0x0C,0x09,0x0F,0x0A,0x0C,0x0B,0x0F,
                    0x00,0x0C,0x09,0x0F,0x08,0x0C,0x0B,0x0F,0x00,0x00,0x0F,0x0B,0x0D,0x09,0x08,0x0C,
                    0x00,0x00,0x0B,0x0F,0x09,0x09,0x0D,0x0C,0x00,0x08,0x0D,0x0E,0x0E,0x0B,0x0F,0x0E,
                    0x00,0x08,0x0C,0x07,0x0E,0x0B,0x0F,0x0E,0x00,0x00,0x07,0x0F,0x00,0x0F,0x09,0x0F,
                    0x00,0x09,0x0A,0x0C,0x09,0x0F,0x0C,0x0D,0x00,0x0B,0x0A,0x0F,0x09,0x0D,0x0C,0x0D,
                    0x00,0x00,0x0D,0x0F,0x0A,0x0F,0x0B,0x0E,0x00,0x08,0x0B,0x0F,0x07,0x0B,0x0D,0x0D,
                    0x00,0x08,0x0A,0x0F,0x07,0x0B,0x0B,0x0D,0x00,0x00,0x0C,0x0D,0x00,0x09,0x07,0x0F,
                    0x00,0x00,0x0C,0x0D,0x00,0x09,0x0C,0x0F,0x00,0x0F,0x0C,0x0F,0x0C,0x0F,0x0F,0x0F,
                    0x00,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x00,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,
                    /* G-1H - character palette blue component */
                    0x00,0x00,0x00,0x00,0x0F,0x0F,0x0F,0x0F,0x0D,0x08,0x07,0x03,0x0C,0x00,0x08,0x0E,
                    0x0B,0x0F,0x08,0x0C,0x06,0x05,0x05,0x0A,0x00,0x00,0x00,0x0F,0x0F,0x08,0x0A,0x0C,
                    0x0D,0x04,0x08,0x0B,0x00,0x0C,0x00,0x08,0x08,0x0C,0x0F,0x07,0x0C,0x0D,0x0C,0x07,
                    0x0D,0x0F,0x0E,0x07,0x07,0x0C,0x07,0x08,0x0C,0x00,0x00,0x00,0x0F,0x00,0x00,0x00,
                    0x03,0x00,0x00,0x08,0x07,0x07,0x0E,0x0F,0x0F,0x07,0x08,0x00,0x00,0x0A,0x0E,0x0F,
                    0x00,0x07,0x07,0x0C,0x0F,0x0E,0x0C,0x07,0x0D,0x0F,0x0F,0x03,0x0D,0x08,0x00,0x0F,
                    0x0D,0x0C,0x00,0x07,0x09,0x08,0x0C,0x0C,0x0F,0x00,0x08,0x0C,0x09,0x0F,0x00,0x0B,
                    0x09,0x00,0x0A,0x09,0x09,0x08,0x00,0x0B,0x09,0x0B,0x0A,0x09,0x0F,0x08,0x0D,0x0B,
                    0x09,0x0B,0x00,0x0C,0x00,0x0D,0x0B,0x0B,0x0D,0x00,0x08,0x0C,0x00,0x0B,0x0D,0x08,
                    0x0D,0x00,0x08,0x0C,0x08,0x0B,0x0F,0x00,0x0D,0x00,0x08,0x0C,0x08,0x0B,0x08,0x00,
                    0x00,0x0F,0x07,0x0A,0x0C,0x0B,0x00,0x00,0x00,0x0F,0x0F,0x0A,0x0F,0x0C,0x0A,0x0B,
                    0x0D,0x09,0x0C,0x0C,0x0F,0x09,0x0E,0x08,0x0F,0x0F,0x0D,0x0F,0x00,0x0F,0x0F,0x0F,
                    0x0F,0x0F,0x00,0x0F,0x00,0x0F,0x0F,0x0F,0x0D,0x00,0x0D,0x00,0x00,0x00,0x0A,0x0F,
                    0x0D,0x00,0x0D,0x0F,0x00,0x09,0x00,0x0F,0x0D,0x0A,0x00,0x00,0x00,0x0B,0x09,0x0A,
                    0x06,0x00,0x00,0x0C,0x0C,0x08,0x0F,0x0A,0x0D,0x00,0x00,0x0C,0x06,0x0A,0x0C,0x00,
                    0x06,0x00,0x00,0x0C,0x00,0x0A,0x00,0x0F,0x0D,0x00,0x0B,0x00,0x0A,0x08,0x0B,0x0C,
                    /* B-1L - sprite palette blue component */
                    0x00,0x00,0x0C,0x09,0x0A,0x0F,0x09,0x0F,0x00,0x00,0x0C,0x09,0x0A,0x0E,0x0F,0x0F,
                    0x00,0x0C,0x09,0x09,0x0A,0x0E,0x00,0x0F,0x00,0x00,0x0F,0x0A,0x00,0x00,0x07,0x09,
                    0x00,0x0C,0x0D,0x0F,0x0D,0x0D,0x09,0x0A,0x00,0x00,0x0D,0x0F,0x0D,0x0D,0x09,0x00,
                    0x00,0x0F,0x0C,0x0E,0x09,0x00,0x0B,0x0F,0x00,0x00,0x0F,0x00,0x0B,0x0A,0x00,0x0B,
                    0x00,0x09,0x0A,0x0E,0x0F,0x07,0x08,0x0D,0x00,0x08,0x0A,0x0E,0x0F,0x07,0x08,0x0F,
                    0x00,0x09,0x07,0x09,0x00,0x0F,0x07,0x0F,0x00,0x00,0x08,0x07,0x0B,0x0F,0x0F,0x0F,
                    0x00,0x00,0x07,0x00,0x07,0x07,0x0E,0x0F,0x00,0x00,0x0F,0x00,0x0B,0x07,0x0F,0x07,
                    0x00,0x00,0x08,0x00,0x0B,0x0C,0x09,0x0F,0x00,0x00,0x08,0x00,0x00,0x0C,0x09,0x0F,
                    0x00,0x00,0x08,0x00,0x0B,0x0C,0x09,0x0F,0x00,0x00,0x09,0x0B,0x00,0x09,0x00,0x0A,
                    0x00,0x00,0x0B,0x0F,0x09,0x09,0x0D,0x0A,0x00,0x00,0x0B,0x00,0x0A,0x0B,0x0B,0x0E,
                    0x00,0x00,0x08,0x00,0x0A,0x0B,0x0B,0x0E,0x00,0x00,0x07,0x00,0x0E,0x0F,0x0C,0x0F,
                    0x00,0x07,0x0A,0x09,0x09,0x0F,0x0C,0x0A,0x00,0x0C,0x0A,0x0F,0x09,0x0D,0x0C,0x0A,
                    0x00,0x00,0x0D,0x00,0x08,0x0F,0x09,0x0E,0x00,0x08,0x08,0x09,0x08,0x09,0x0B,0x0D,
                    0x00,0x08,0x0A,0x09,0x08,0x09,0x0B,0x0D,0x00,0x09,0x09,0x0B,0x00,0x00,0x08,0x0F,
                    0x00,0x09,0x09,0x0B,0x00,0x00,0x0C,0x0F,0x00,0x0F,0x00,0x00,0x0C,0x0F,0x0F,0x0F,
                    0x00,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x00,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F
            };



            static MachineDriver machine_driver = new MachineDriver
           (
		/* basic machine hardware */
		new MachineCPU[] {
			new MachineCPU(
                                    CPU_Z80,
                                    4000000,	/* 4 Mhz (?) */
                                    0,
                                    readmem,writemem,readport, writeport,
                                    interrupt,1
                            ),
                    },
                    60,
                    null,

                    /* video hardware */
                    32*8, 32*8,  new rectangle( 0*8, 32*8-1, 0*8, 32*8-1 ),
                    gfxdecodeinfo,
                    256,32*8+32*8,
                    kungfum_vh_convert_color_prom,
                    VIDEO_TYPE_RASTER,
                    null,
                    kungfum_vh_start,
                    kungfum_vh_stop,
                    kungfum_vh_screenrefresh,

                    /* sound hardware */
                    null,
                    kungfum_sh_init,
                    null,
                    null,
                    kungfum_sh_update
            );



            /***************************************************************************

              Game driver(s)

            ***************************************************************************/
            static RomLoadPtr kungfum_rom = new RomLoadPtr(){ public void handler()  
            {

                   ROM_REGION(0x10000);	/* 64k for code */
                      ROM_LOAD( "a-4e-c.bin",   0x0000, 0x4000, 0xb6e2d083 );
                      ROM_LOAD( "a-4d-c.bin",   0x4000, 0x4000, 0x7532918e );
        
                   ROM_REGION(0x1e000);	/* temporary space for graphics (disposed after conversion) */
                   ROM_LOAD( "g-4e-a.bin", 0x00000, 0x2000, 0x6b2cc9c8 );	/* characters */
                   ROM_LOAD( "g-4d-a.bin", 0x02000, 0x2000, 0xc648f558 );
                   ROM_LOAD( "g-4c-a.bin", 0x04000, 0x2000, 0xfbe9276e );                 
                   ROM_LOAD( "b-3n-.bin",  0x06000, 0x2000, 0x28a213aa );	/* sprites */
                   ROM_LOAD( "b-4n-.bin",  0x08000, 0x2000, 0xd5228df3 );
                   ROM_LOAD( "b-4m-.bin",  0x0a000, 0x2000, 0xb16de4f2 );
                   ROM_LOAD( "b-3m-.bin",  0x0c000, 0x2000, 0xeba0d66b );
                   ROM_LOAD( "b-4k-.bin",  0x0e000, 0x2000, 0x16fb5150 );
                   ROM_LOAD( "b-4f-.bin",  0x10000, 0x2000, 0x67745a33 );
                   ROM_LOAD( "b-4l-.bin",  0x12000, 0x2000, 0xbd1c2261 );
                   ROM_LOAD( "b-4h-.bin",  0x14000, 0x2000, 0x8ac5ed3a );
                   ROM_LOAD( "b-4c-.bin",  0x16000, 0x2000, 0x01298885 );
                   ROM_LOAD( "b-4e-.bin",  0x18000, 0x2000, 0xc77b87d4 );
                   ROM_LOAD( "b-4d-.bin",  0x1a000, 0x2000, 0x6a70615f);
                   ROM_LOAD( "b-4a-.bin",  0x1c000, 0x2000, 0x6189d626 );

                   ROM_REGION(0x4000);	/* samples (ADPCM 4-bit) */
                   ROM_LOAD( "a-3e-.bin", 0x0000, 0x2000, 0x58e87ab0 );
                   ROM_LOAD( "a-3f-.bin", 0x2000, 0x2000, 0xc81e31ea );

                   ROM_REGION(0x10000);	/* 64k for the audio CPU (6803) */
                   ROM_LOAD( "a-3h-.bin", 0xe000, 0x2000, 0xd99fb995 );
                   ROM_END();
            }};
            static RomLoadPtr kungfub2_rom = new RomLoadPtr(){ public void handler()  
            {
                   ROM_REGION(0x10000);	/* 64k for code */
                   ROM_LOAD( "kf4", 0x0000, 0x4000, 0x3f65313f );
                   ROM_LOAD( "kf5", 0x4000, 0x4000, 0x9ea325f3 );

                    ROM_REGION(0x1e000);	/* temporary space for graphics (disposed after conversion) */
                   ROM_LOAD( "g-4e-a.bin", 0x00000, 0x2000, 0x6b2cc9c8 );	/* characters */
                   ROM_LOAD( "g-4d-a.bin", 0x02000, 0x2000, 0xc648f558 );
                   ROM_LOAD( "g-4c-a.bin", 0x04000, 0x2000, 0xfbe9276e );                 
                   ROM_LOAD( "b-3n-.bin",  0x06000, 0x2000, 0x28a213aa );	/* sprites */
                   ROM_LOAD( "b-4n-.bin",  0x08000, 0x2000, 0xd5228df3 );
                   ROM_LOAD( "b-4m-.bin",  0x0a000, 0x2000, 0xb16de4f2 );
                   ROM_LOAD( "b-3m-.bin",  0x0c000, 0x2000, 0xeba0d66b );
                   ROM_LOAD( "b-4k-.bin",  0x0e000, 0x2000, 0x16fb5150 );
                   ROM_LOAD( "b-4f-.bin",  0x10000, 0x2000, 0x67745a33 );
                   ROM_LOAD( "b-4l-.bin",  0x12000, 0x2000, 0xbd1c2261 );
                   ROM_LOAD( "b-4h-.bin",  0x14000, 0x2000, 0x8ac5ed3a );
                   ROM_LOAD( "b-4c-.bin",  0x16000, 0x2000, 0x01298885 );
                   ROM_LOAD( "b-4e-.bin",  0x18000, 0x2000, 0xc77b87d4 );
                   ROM_LOAD( "b-4d-.bin",  0x1a000, 0x2000, 0x6a70615f);
                   ROM_LOAD( "b-4a-.bin",  0x1c000, 0x2000, 0x6189d626 );

                   ROM_REGION(0x4000);	/* samples (ADPCM 4-bit) */
                   ROM_LOAD( "a-3e-.bin", 0x0000, 0x2000, 0x58e87ab0 );
                   ROM_LOAD( "a-3f-.bin", 0x2000, 0x2000, 0xc81e31ea );

                   ROM_REGION(0x10000);	/* 64k for the audio CPU (6803) */
                   ROM_LOAD( "a-3h-.bin", 0xe000, 0x2000, 0xd99fb995 );
                   ROM_END();
        }};



        static HiscoreLoadPtr hiload = new HiscoreLoadPtr() { public int handler()
	{
                    /* get RAM pointer (this game is multiCPU, we can't assume the global */
                    /* RAM pointer is pointing to the right place) */
                    char []RAM = Machine.memory_region[0];

                    if (memcmp(RAM, 0xe801, new char[] { 0x00, 0x14, 0x95}, 3) == 0 &&
				memcmp(RAM, 0xe981, new char[] { 0x00, 0x48, 0x52}, 3) == 0)
                    {
                            FILE f;


                            if ((f = fopen(name, "rb")) != null)
                            {
                                    fread(RAM,0xea06,1,6*20,f);
                                    RAM[0xe980] = RAM[0xea7a];
                                    RAM[0xe981] = RAM[0xea79];
                                    RAM[0xe982] = RAM[0xea78];
                                    fclose(f);
                            }

                            return 1;
                    }
                    else return 0;	/* we can't load the hi scores yet */
            } };



         static HiscoreSavePtr hisave = new HiscoreSavePtr() { public void handler()
	 {
		FILE f;
                    /* get RAM pointer (this game is multiCPU, we can't assume the global */
                    /* RAM pointer is pointing to the right place) */
                    char []RAM = Machine.memory_region[0];


                    if ((f = fopen(name, "wb")) != null)
                    {
                            fwrite(RAM,0xea06,1,6*20,f);
                            fclose(f);
                    }
            } };



             public static GameDriver  kungfum_driver =new GameDriver
            (
                    "Kung Fu Master",
                    "kungfum",
                   "MIRKO BUFFONI\nNICOLA SALMORIA\nISHMAIR\nPAUL SWAN",
                    machine_driver,

                    kungfum_rom,
                    null, null,
                    null,

                    input_ports,null, trak_ports, dsw, keys,

                    color_prom, null, null,
                    ORIENTATION_DEFAULT,

                    hiload, hisave
            );

             public static GameDriver  kungfub2_driver  =new GameDriver
            (
                    "Kung Fu Master (bootleg)",
                    "kungfub2",
                    "MIRKO BUFFONI\nNICOLA SALMORIA\nISHMAIR\nPAUL SWAN",
                    machine_driver,

                    kungfub2_rom,
                    null, null,
                    null,

                    input_ports,null, trak_ports, dsw, keys,

                    color_prom, null, null,
                    ORIENTATION_DEFAULT,

                    hiload, hisave
            );
}
