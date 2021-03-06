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
 * ported to v0.28
 * ported to v0.27
 *
 *  NOTES: romsets are from v0.36 roms
 *
 */
package drivers;

import static arcadeflex.libc.*;
import static mame.commonH.*;
import static mame.cpuintrf.*;
import static mame.driverH.*;
import static mame.mame.*;
import static mame.osdependH.*;
import static sndhrdw._8910intf.*;
import static sndhrdw.vulgus.*;
import static sndhrdw.generic.*;
import static machine.vulgus.*;
import static vidhrdw.generic.*;
import static vidhrdw.vulgus.*;
import static machine._1942.*;
import static mame.inptport.*;
import static mame.memoryH.*;

public class vulgus {
    static MemoryReadAddress readmem[] =
    {
            new MemoryReadAddress( 0xd000, 0xefff, MRA_RAM ),
            new MemoryReadAddress( 0xc000, 0xc000, input_port_0_r ),
            new MemoryReadAddress( 0xc001, 0xc001, input_port_1_r ),
            new MemoryReadAddress( 0xc002, 0xc002, input_port_2_r ),
            new MemoryReadAddress( 0xc003, 0xc003, input_port_3_r ),
            new MemoryReadAddress( 0xc004, 0xc004, input_port_4_r ),
            new MemoryReadAddress( 0x0000, 0x9fff, MRA_ROM ),
            new MemoryReadAddress( -1 )	/* end of table */
    };

    static MemoryWriteAddress writemem[] =
    {
            new MemoryWriteAddress( 0xe000, 0xefff, MWA_RAM ),
            new MemoryWriteAddress( 0xd000, 0xd3ff, videoram_w, videoram, videoram_size ),
            new MemoryWriteAddress( 0xd400, 0xd7ff, colorram_w, colorram ),
            new MemoryWriteAddress( 0xd800, 0xdbff, vulgus_bgvideoram_w, vulgus_bgvideoram, vulgus_bgvideoram_size ),
            new MemoryWriteAddress( 0xdc00, 0xdfff, vulgus_bgcolorram_w, vulgus_bgcolorram ),
            new MemoryWriteAddress( 0xcc00, 0xcc7f, MWA_RAM, spriteram, spriteram_size ),
            new MemoryWriteAddress( 0xc802, 0xc803, MWA_RAM, vulgus_scrolllow ),
            new MemoryWriteAddress( 0xc902, 0xc903, MWA_RAM, vulgus_scrollhigh ),
            new MemoryWriteAddress( 0xc800, 0xc800, sound_command_w ),
            new MemoryWriteAddress( 0x0000, 0x9fff, MWA_ROM ),
            new MemoryWriteAddress( -1 )	/* end of table */
    };



    static MemoryReadAddress sound_readmem[] =
    {
            new MemoryReadAddress( 0x4000, 0x47ff, MRA_RAM ),
            new MemoryReadAddress( 0x6000, 0x6000, sound_command_latch_r ),
            new MemoryReadAddress( 0x0000, 0x1fff, MRA_ROM ),
            new MemoryReadAddress( -1 )	/* end of table */
    };

    static MemoryWriteAddress sound_writemem[] =
    {
            new MemoryWriteAddress( 0x4000, 0x47ff, MWA_RAM ),
            new MemoryWriteAddress( 0x8000, 0x8000, AY8910_control_port_0_w ),
            new MemoryWriteAddress( 0x8001, 0x8001, AY8910_write_port_0_w ),
            new MemoryWriteAddress( 0xc000, 0xc000, AY8910_control_port_1_w ),
            new MemoryWriteAddress( 0xc001, 0xc001, AY8910_write_port_1_w ),
            new MemoryWriteAddress( 0x0000, 0x1fff, MWA_ROM ),
            new MemoryWriteAddress( -1 )	/* end of table */
    };



    static InputPort input_ports[] =
    {
            new InputPort(	/* IN0 */
                    0xff,
                    new int[]{ OSD_KEY_1, OSD_KEY_2, 0, 0, OSD_KEY_3, 0, 0, 0 }
            ),
		new InputPort(	/* IN1 */
                    0xff,
                    new int[]{ OSD_KEY_RIGHT, OSD_KEY_LEFT, OSD_KEY_DOWN, OSD_KEY_UP,
                                    OSD_KEY_CONTROL, OSD_KEY_ALT, 0, 0 }
            ),
		new InputPort(	/* IN2 */
                    0xff,
                    new int[]{ 0, 0, 0, 0, 0, 0, 0, 0 }
            ),
		new InputPort(	/* DSW1 */
                    0xf7,
                    new int[]{ 0, 0, 0, 0, 0, 0, 0, 0 }
            ),
		new InputPort(	/* DSW2 */
                    0xff,
                    new int[]{ 0, 0, 0, OSD_KEY_F2, 0, 0, 0, 0 }
            ),
		new InputPort( -1 )	/* end of table */
    };

    static TrakPort[] trak_ports =
       {
           new TrakPort(-1)
       };


    static KEYSet keys[] =
    {
             new KEYSet( 1, 3, "MOVE UP" ),
             new KEYSet( 1, 1, "MOVE LEFT"  ),
             new KEYSet( 1, 0, "MOVE RIGHT" ),
             new KEYSet( 1, 2, "MOVE DOWN" ),
             new KEYSet( 1, 4, "FIRE" ),
             new KEYSet( 1, 5, "MISSILE" ),
             new KEYSet( -1 )
    };


    static DSW dsw[] =
    {
            new DSW( 3, 0x03, "LIVES", new String[]{ "5", "1", "2", "3" }, 1 ),
            new DSW( 4, 0x30, "BONUS", new String[]{ "30000 70000", "10000 60000", "20000 70000", "20000 60000" }, 1 ),
            new DSW( 4, 0x03, "DIFFICULTY", new String[]{ "HARDEST", "HARD", "NORMAL", "EASY" }, 1 ),
    /* not sure about difficulty. Code perform a read and (& 0x03). NdMix */
            new DSW( -1 )
    };



    static GfxLayout charlayout = new GfxLayout
	(
            8,8,	/* 8*8 characters */
            512,	/* 512 characters */
            2,	/* 2 bits per pixel */
            new int[]{ 4, 0 },
            new int[]{ 0*16, 1*16, 2*16, 3*16, 4*16, 5*16, 6*16, 7*16 },
            new int[]{ 8+3, 8+2, 8+1, 8+0, 3, 2, 1, 0 },
            16*8	/* every char takes 16 consecutive bytes */
    );
    static GfxLayout tilelayout = new GfxLayout
	(
            16,16,	/* 16*16 tiles */
            256,	/* 256 tiles */
            3,	/* 3 bits per pixel */
            new int[]{ 0, 0x2000*8, 0x4000*8 },	/* the bitplanes are separated */
            new int[]{ 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8,
                            8*8, 9*8, 10*8, 11*8, 12*8, 13*8, 14*8, 15*8 },
            new int[]{ 16*8+7, 16*8+6, 16*8+5, 16*8+4, 16*8+3, 16*8+2, 16*8+1, 16*8+0,
                            7, 6, 5, 4, 3, 2, 1, 0 },
            32*8	/* every tile takes 32 consecutive bytes */
    );
    static GfxLayout spritelayout = new GfxLayout
	(
            16,16,	/* 16*16 sprites */
            128,	/* 128 sprites */
            4,	/* 4 bits per pixel */
            new int[]{ 0x2000*8+4, 0x2000*8, 4, 0 },
            new int[]{ 0*16, 1*16, 2*16, 3*16, 4*16, 5*16, 6*16, 7*16,
                            8*16, 9*16, 10*16, 11*16, 12*16, 13*16, 14*16, 15*16 },
           new int[] { 33*8+3, 33*8+2, 33*8+1, 33*8+0, 32*8+3, 32*8+2, 32*8+1, 32*8+0,
                            8+3, 8+2, 8+1, 8+0, 3, 2, 1, 0 },
            64*8	/* every sprite takes 64 consecutive bytes */
    );



    static GfxDecodeInfo gfxdecodeinfo[] =
    {
            new GfxDecodeInfo( 1, 0x00000, charlayout,           0, 64 ),
            new GfxDecodeInfo( 1, 0x02000, tilelayout,  64*4+16*16, 32*4 ),
            new GfxDecodeInfo( 1, 0x08000, tilelayout,  64*4+16*16, 32*4 ),
            new GfxDecodeInfo( 1, 0x0e000, spritelayout,      64*4, 16 ),
            new GfxDecodeInfo( 1, 0x12000, spritelayout,      64*4, 16 ),
            new GfxDecodeInfo( -1 ) /* end of array */
    };



    /* these are NOT the original color PROMs */
    static char color_prom[] =
    {
            /* 08E_SB-5: palette red component */
            0x00,0x00,0x00,0x04,0x06,0x07,0x00,0x08,0x05,0x00,0x08,0x00,0x07,0x00,0x00,0x00,
            0x00,0x00,0x00,0x04,0x06,0x07,0x00,0x08,0x05,0x06,0x08,0x07,0x00,0x05,0x00,0x00,
            0x00,0x09,0x08,0x06,0x05,0x04,0x07,0x08,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
            0x00,0x00,0x00,0x07,0x06,0x07,0x00,0x08,0x05,0x06,0x08,0x00,0x07,0x00,0x00,0x00,
            0x00,0x0B,0x09,0x07,0x05,0x03,0x0C,0x0A,0x08,0x06,0x04,0x0E,0x0E,0x0D,0x07,0x00,
            0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
            0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
            0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
            0x00,0x08,0x05,0x0B,0x0A,0x0A,0x0B,0x0C,0x0C,0x0E,0x0C,0x00,0x03,0x04,0x06,0x00,
            0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
            0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
            0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
            0x00,0x08,0x05,0x0B,0x0A,0x0A,0x0B,0x0C,0x0C,0x0E,0x0C,0x00,0x03,0x04,0x06,0x00,
            0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
            0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
            0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
            /* 09E_SB-6: palette green component */
            0x00,0x05,0x08,0x06,0x05,0x06,0x07,0x08,0x04,0x05,0x07,0x07,0x07,0x06,0x05,0x06,
            0x00,0x05,0x08,0x06,0x05,0x06,0x07,0x08,0x05,0x06,0x07,0x07,0x04,0x06,0x05,0x06,
            0x00,0x09,0x08,0x07,0x06,0x05,0x06,0x07,0x05,0x06,0x04,0x00,0x00,0x00,0x05,0x06,
            0x00,0x05,0x08,0x06,0x05,0x06,0x07,0x08,0x04,0x05,0x07,0x07,0x07,0x06,0x05,0x06,
            0x00,0x0E,0x0C,0x0A,0x07,0x05,0x0C,0x0A,0x08,0x06,0x04,0x0C,0x09,0x00,0x00,0x00,
            0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
            0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
            0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
            0x00,0x0A,0x09,0x0B,0x05,0x09,0x00,0x05,0x09,0x0E,0x0C,0x0A,0x0D,0x06,0x0A,0x00,
            0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
            0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
            0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
            0x00,0x0A,0x09,0x0B,0x05,0x09,0x00,0x05,0x09,0x0E,0x0C,0x0A,0x0D,0x06,0x0A,0x00,
            0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
            0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
            0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
            /* 10E_SB-7: palette blue component */
            0x00,0x00,0x00,0x00,0x04,0x05,0x00,0x08,0x03,0x00,0x06,0x08,0x07,0x07,0x08,0x09,
            0x00,0x00,0x00,0x00,0x04,0x05,0x00,0x08,0x05,0x06,0x06,0x07,0x05,0x06,0x08,0x09,
            0x00,0x09,0x08,0x07,0x06,0x05,0x05,0x06,0x08,0x09,0x07,0x00,0x00,0x00,0x08,0x09,
            0x00,0x00,0x00,0x05,0x04,0x05,0x00,0x08,0x03,0x04,0x06,0x08,0x07,0x07,0x08,0x09,
            0x00,0x00,0x00,0x00,0x00,0x00,0x09,0x07,0x05,0x03,0x01,0x00,0x00,0x00,0x00,0x00,
            0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
            0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
            0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
            0x00,0x0D,0x0B,0x0B,0x08,0x0F,0x00,0x00,0x00,0x0E,0x00,0x00,0x0B,0x00,0x00,0x00,
            0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
            0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
            0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
            0x00,0x0D,0x0B,0x0B,0x08,0x0F,0x00,0x00,0x00,0x0E,0x00,0x00,0x0B,0x00,0x00,0x00,
            0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
            0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
            0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
            /* F01_SB-0: char lookup table */
            0x0F,0x01,0x02,0x03,0x0F,0x02,0x03,0x04,0x0F,0x03,0x04,0x05,0x0F,0x04,0x05,0x06,
            0x0F,0x05,0x06,0x07,0x0F,0x06,0x07,0x08,0x0F,0x07,0x08,0x09,0x0F,0x08,0x09,0x0A,
            0x0F,0x09,0x0A,0x0B,0x0F,0x0A,0x0B,0x0C,0x0F,0x0B,0x0C,0x0D,0x0F,0x0C,0x0D,0x0E,
            0x0F,0x0D,0x0E,0x0F,0x0F,0x0E,0x0F,0x01,0x0F,0x0F,0x01,0x02,0x0F,0x0F,0x0F,0x0F,
            0x0F,0x00,0x00,0x00,0x0F,0x01,0x01,0x01,0x0F,0x02,0x02,0x02,0x0F,0x03,0x03,0x03,
            0x0F,0x04,0x04,0x04,0x0F,0x05,0x05,0x05,0x0F,0x06,0x06,0x06,0x0F,0x07,0x07,0x07,
            0x0F,0x08,0x08,0x08,0x0F,0x09,0x09,0x09,0x0F,0x0A,0x0A,0x0A,0x0F,0x0B,0x0B,0x0B,
            0x0F,0x0C,0x0C,0x0C,0x0F,0x0D,0x0D,0x0D,0x0F,0x0E,0x0E,0x0E,0x0F,0x0F,0x0F,0x0F,
            0x0F,0x03,0x01,0x04,0x0F,0x04,0x03,0x05,0x0F,0x05,0x04,0x06,0x0F,0x06,0x05,0x07,
            0x0F,0x07,0x06,0x08,0x0F,0x08,0x07,0x0A,0x0F,0x0A,0x08,0x0B,0x0F,0x0B,0x0A,0x0E,
            0x0F,0x0E,0x0B,0x0D,0x0F,0x0D,0x0E,0x0C,0x0F,0x0C,0x0D,0x09,0x0F,0x09,0x0C,0x02,
            0x0F,0x02,0x09,0x01,0x0F,0x01,0x02,0x03,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,
            0x0F,0x09,0x0D,0x0E,0x0F,0x04,0x0D,0x0A,0x0F,0x01,0x02,0x09,0x0F,0x01,0x00,0x03,
            0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,
            0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,
            0x0F,0x09,0x09,0x09,0x0F,0x0A,0x0A,0x0A,0x0F,0x0A,0x06,0x02,0x0F,0x0C,0x0F,0x0F,
            /* 03K_SB-8: sprite lookup table */
            0x00,0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08,0x09,0x0A,0x06,0x08,0x0D,0x0E,0x0F,
            0x00,0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08,0x09,0x0A,0x08,0x06,0x0D,0x0E,0x0F,
            0x00,0x06,0x07,0x08,0x09,0x0A,0x06,0x07,0x08,0x09,0x0A,0x06,0x08,0x0D,0x0E,0x0F,
            0x00,0x06,0x07,0x08,0x09,0x0A,0x06,0x07,0x08,0x09,0x0A,0x08,0x06,0x0D,0x0E,0x0F,
            0x00,0x0B,0x0C,0x0D,0x0E,0x0E,0x06,0x07,0x08,0x09,0x0A,0x06,0x08,0x0D,0x0E,0x0F,
            0x00,0x0B,0x0C,0x0D,0x0E,0x0E,0x06,0x07,0x08,0x09,0x0A,0x08,0x06,0x0D,0x0E,0x0F,
            0x00,0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08,0x09,0x0A,0x0B,0x0C,0x0D,0x0E,0x0F,
            0x00,0x06,0x07,0x08,0x09,0x0A,0x06,0x07,0x08,0x09,0x0A,0x0B,0x0C,0x0D,0x0E,0x0F,
            0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,
            0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,
            0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,
            0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,
            0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,
            0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,
            0x00,0x06,0x08,0x09,0x04,0x05,0x06,0x07,0x08,0x09,0x0A,0x0B,0x0C,0x0D,0x0E,0x0F,
            0x00,0x08,0x06,0x09,0x04,0x05,0x06,0x07,0x08,0x09,0x0A,0x0B,0x0C,0x0D,0x0E,0x0F,
            /* 06D_SB-4: tile lookup table */
            0x00,0x01,0x02,0x03,0x04,0x05,0x08,0x0A,0x00,0x01,0x07,0x0C,0x04,0x05,0x0E,0x0F,
            0x00,0x01,0x02,0x03,0x04,0x05,0x06,0x0A,0x00,0x01,0x0D,0x03,0x04,0x0E,0x09,0x0F,
            0x00,0x01,0x0A,0x03,0x04,0x05,0x06,0x08,0x00,0x01,0x02,0x0E,0x0F,0x05,0x06,0x07,
            0x00,0x01,0x08,0x03,0x0B,0x0E,0x06,0x0F,0x00,0x01,0x07,0x0B,0x09,0x0E,0x0D,0x0F,
            0x00,0x01,0x0F,0x0C,0x04,0x05,0x06,0x07,0x00,0x01,0x0A,0x0F,0x04,0x0E,0x06,0x07,
            0x00,0x01,0x02,0x03,0x04,0x05,0x06,0x09,0x00,0x0A,0x02,0x09,0x08,0x05,0x06,0x07,
            0x00,0x08,0x0A,0x03,0x04,0x05,0x0D,0x0B,0x00,0x01,0x02,0x03,0x0C,0x05,0x06,0x0D,
            0x00,0x01,0x02,0x03,0x05,0x08,0x09,0x0A,0x00,0x08,0x02,0x09,0x04,0x05,0x06,0x07,
            0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,
            0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,
            0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,
            0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,
            0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,
            0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,
            0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,0x0F,
            0x00,0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x00,0x0D,0x08,0x09,0x0A,0x0B,0x0C,0x0E,
    };



    static MachineDriver machine_driver = new MachineDriver
	(
            /* basic machine hardware */
            new MachineCPU[] {
			new MachineCPU(
                            CPU_Z80,
                            4000000,	/* 4 Mhz (?) */
                            0,
                            readmem,writemem,null, null,
                            c1942_interrupt,2
                    ),
			new MachineCPU(
                            CPU_Z80 | CPU_AUDIO_CPU,
                            3000000,	/* 3 Mhz ??? */
                            2,	/* memory region #2 */
                            sound_readmem,sound_writemem,null, null,
                            vulgus_sh_interrupt,8
                    )
            },
            60,
            null,

            /* video hardware */
            32*8, 32*8, new rectangle( 2*8, 30*8-1, 0*8, 32*8-1 ),
            gfxdecodeinfo,
            256,64*4+16*16+4*32*8,
            vulgus_vh_convert_color_prom,
            VIDEO_TYPE_RASTER,
            null,
            vulgus_vh_start,
            vulgus_vh_stop,
            vulgus_vh_screenrefresh,

            /* sound hardware */
            null,
            null,
            vulgus_sh_start,
            AY8910_sh_stop,
            AY8910_sh_update
    );



    /***************************************************************************

      Game driver(s)

    ***************************************************************************/
    static RomLoadPtr vulgus_rom= new RomLoadPtr(){ public void handler()  
    {
       ROM_REGION(0x1c000);	/* 64k for code */
           ROM_LOAD( "v2",           0x0000, 0x2000, 0x3e18ff62 );
           ROM_LOAD( "v3",           0x2000, 0x2000, 0xb4650d82 );
           ROM_LOAD( "v4",           0x4000, 0x2000, 0x5b26355c );
           ROM_LOAD( "v5",           0x6000, 0x2000, 0x4ca7f10e );
           ROM_LOAD( "1-8n.bin",     0x8000, 0x2000, 0x6ca5ca41 );

            ROM_REGION(0x16000);	/* temporary space for graphics (disposed after conversion) */   
           ROM_LOAD( "1-3d.bin",     0x00000, 0x2000, 0x8bc5d7a5 );	/* characters */	
           ROM_LOAD( "2-2a.bin",     0x02000, 0x2000, 0xe10aaca1 );	/* tiles */
           ROM_LOAD( "2-4a.bin",     0x04000, 0x2000, 0x206a13f1 );
           ROM_LOAD( "2-6a.bin",     0x05000, 0x2000, 0x5a26b38f );      
           ROM_LOAD( "2-3a.bin",     0x08000, 0x2000, 0x8da520da );
           ROM_LOAD( "2-5a.bin",     0x0a000, 0x2000, 0xb6d81984 );	
           ROM_LOAD( "2-7a.bin",     0x0c000, 0x2000, 0x1e1ca773 );
           ROM_LOAD( "2-2n.bin",     0x0e000, 0x2000, 0x6db1b10d );	/* sprites */
           ROM_LOAD( "2-4n.bin",     0x10000, 0x2000, 0x0071a2e3 );     
           ROM_LOAD( "2-3n.bin",     0x12000, 0x2000, 0x5d8c34ec );
           ROM_LOAD( "2-5n.bin",     0x14000, 0x2000, 0x4023a1ec );

             ROM_REGION(0x10000);	/* 64k for the audio CPU */
            ROM_LOAD( "1-11c.bin",    0x0000, 0x2000, 0x3bd2acf4 );
             ROM_END();
        }};



    	static HiscoreLoadPtr hiload = new HiscoreLoadPtr() { public int handler()
	{
            /* get RAM pointer (this game is multiCPU, we can't assume the global */
            /* RAM pointer is pointing to the right place) */
            char []RAM = Machine.memory_region[0];


            /* check if the hi score table has already been initialized */
        /*TOFIX    if (memcmp(RAM, 0xee00, new char[] { 0x00, 0x50, 0x00 }, 3) == 0 &&
				memcmp(RAM, 0xee34, new char[] { 0x00, 0x32, 0x50 }, 3) == 0)
            {
                    FILE f;


                    if ((f = fopen(name,"rb")) != null)
                    {
                            fread(RAM,0xee00,1,13*5,f);
                            RAM[0xee47] = RAM[0xee00];
                            RAM[0xee48] = RAM[0xee01];
                            RAM[0xee49] = RAM[0xee02];
                            fclose(f);
                    }

                    return 1;
            }
            else */return 0;	/* we can't load the hi scores yet */
    }};



         static HiscoreSavePtr hisave = new HiscoreSavePtr() { public void handler()
	{
            FILE f;
            /* get RAM pointer (this game is multiCPU, we can't assume the global */
            /* RAM pointer is pointing to the right place) */
            char []RAM = Machine.memory_region[0];


           /*TOFIX if ((f = fopen(name,"wb")) != null)
            {
                    fwrite(RAM,0xee00,1,13*5,f);
                    fclose(f);
            }*/
    }};



    public static GameDriver vulgus_driver = new GameDriver
        (
            "Vulgus",
            "vulgus",
            "PAUL LEAMAN\nMIRKO BUFFONI\nNICOLA SALMORIA",
            machine_driver,

            vulgus_rom,
            null, null,
            null,

            input_ports,null, trak_ports, dsw, keys,

            color_prom, null, null,
           ORIENTATION_DEFAULT,

            hiload, hisave
    );
   
}
