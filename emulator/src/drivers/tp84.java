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
 * 
 *
 *
 *  uses romset from v0.36
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
import static machine.tp84.*;
import static vidhrdw.tp84.*;
import static sndhrdw.tp84.*;
import static mame.memoryH.*;

public class tp84 {

        /* CPU 1 read addresses */
        static MemoryReadAddress readmem[] =
        {
                new MemoryReadAddress( 0x2800, 0x2800, input_port_0_r ),
                new MemoryReadAddress( 0x2820, 0x2820, input_port_1_r ),
                new MemoryReadAddress( 0x2840, 0x2840, input_port_2_r ),
                new MemoryReadAddress( 0x2860, 0x2860, input_port_3_r ),
                new MemoryReadAddress( 0x3000, 0x3000, input_port_4_r ),
                new MemoryReadAddress( 0x4000, 0x4fff, MRA_RAM ),
                new MemoryReadAddress( 0x523a, 0x523a, tp84_catchloop_r ), /* JB 970829 */
                new MemoryReadAddress( 0x5000, 0x57ff, tp84_sharedram_r ),
                new MemoryReadAddress( 0x8000, 0xffff, MRA_ROM ),
                new MemoryReadAddress( -1 )	/* end of table */
        };

        /* CPU 1 write addresses */
        static MemoryWriteAddress writemem[] =
        {
                new MemoryWriteAddress( 0x2000, 0x2000, MWA_RAM ), /*Watch dog?*/
                new MemoryWriteAddress( 0x2800, 0x2800, tp84_col0_w ),
                new MemoryWriteAddress( 0x3000, 0x3000, MWA_RAM ),
                new MemoryWriteAddress( 0x3a00, 0x3a00, sound_command_w ),
                new MemoryWriteAddress( 0x3c00, 0x3c00, MWA_RAM, tp84_scrollx ), /* Y scroll */
                new MemoryWriteAddress( 0x3e00, 0x3e00, MWA_RAM, tp84_scrolly ), /* X scroll */
                new MemoryWriteAddress( 0x4000, 0x43ff, videoram_w, videoram , videoram_size),
                new MemoryWriteAddress( 0x4400, 0x47ff, tp84_videoram2_w, tp84_videoram2 ),
                new MemoryWriteAddress( 0x4800, 0x4bff, colorram_w, colorram ),
                new MemoryWriteAddress( 0x4c00, 0x4fff, tp84_colorram2_w, tp84_colorram2 ),
                new MemoryWriteAddress( 0x5000, 0x57ff, tp84_sharedram_w, tp84_sharedram ),   /* 0x5000-0x517f sprites definitions*/
                new MemoryWriteAddress( 0x8000, 0xffff, MWA_ROM ),
                new MemoryWriteAddress( -1 )	/* end of table */
        };


        /* CPU 2 read addresses */
        static MemoryReadAddress readmem_cpu2[] =
        {
                new MemoryReadAddress( 0x0000, 0x0000, MRA_RAM ),
                new MemoryReadAddress( 0x2000, 0x2000, tp84_beam_r ), /* beam position */
                new MemoryReadAddress( 0x6000, 0x7fff, MRA_RAM ),
                new MemoryReadAddress( 0x8000, 0x87ff, tp84_sharedram_r ),  /* shared RAM with the main CPU */
                new MemoryReadAddress( 0xe000, 0xffff, MRA_ROM ),
                new MemoryReadAddress( -1 )	/* end of table */
        };

        /* CPU 2 write addresses */
        static MemoryWriteAddress writemem_cpu2[] =
        {
                new MemoryWriteAddress( 0x0000, 0x0000, MWA_RAM ), /* Watch dog ?*/
                new MemoryWriteAddress( 0x4000, 0x4000, tp84_catchloop_w ), /* IRQ enable */ /* JB 970829 */
                new MemoryWriteAddress( 0x6000, 0x7fff, MWA_RAM ),
                new MemoryWriteAddress( 0x8000, 0x87ff, tp84_sharedram_w ),    /* shared RAM with the main CPU */
                new MemoryWriteAddress( 0xe000, 0xffff, MWA_ROM ),              /* ROM code */

                new MemoryWriteAddress( -1 )	/* end of table */
        };


        static MemoryReadAddress sound_readmem[] =
        {
                new MemoryReadAddress( 0x0000, 0x1fff, MRA_ROM ),
                new MemoryReadAddress( 0x4000, 0x43ff, MRA_RAM ),
                new MemoryReadAddress( 0x6000, 0x6000, sound_command_r ),
                new MemoryReadAddress( 0x8000, 0x8000, tp84_sh_timer_r ),
                new MemoryReadAddress( -1 )	/* end of table */
        };

        static MemoryWriteAddress sound_writemem[] =
        {
                new MemoryWriteAddress( 0x0000, 0x1fff, MWA_ROM ),
                new MemoryWriteAddress( 0x4000, 0x43ff, MWA_RAM ),
                new MemoryWriteAddress( 0xc000, 0xc000, MWA_NOP ),
                new MemoryWriteAddress( 0xc001, 0xc001, tp84_sound1_w ),
                new MemoryWriteAddress( 0xc003, 0xc003, tp84_sound1_w ),
                new MemoryWriteAddress( 0xc004, 0xc004, tp84_sound1_w ),
                new MemoryWriteAddress( -1 )	/* end of table */
        };



        static InputPort TP84_input_ports[] =
        {
                new InputPort(	/* Insert Coin */
                        0xFF,	/* default_value */
                        new int[]{ OSD_KEY_3, 0, 0, OSD_KEY_1, OSD_KEY_2, 0, 0, 0 }
                ),
                new InputPort(	/* Player 1 joystick */
                        0xFF,	/* default_value */
                       new int[] { OSD_KEY_LEFT, OSD_KEY_RIGHT, OSD_KEY_UP, OSD_KEY_DOWN, OSD_KEY_CONTROL, OSD_KEY_ALT, 0, 0}
                ),
                new InputPort(	/* Second player joystick */
                        0xFF,	/* default_value */
                        new int[]{ 0, 0, 0, 0, 0, 0, 0, 0 }
                ),
                new InputPort(	/* Dip 1 Self test and pricing */
                        0xff,	/* default_value */
                        new int[]{ 0, 0, 0, 0, 0, 0, 0, 0 }
                ),
                new InputPort(	/* Dip 2 */
                        0xD5,	/* default_value */
                        new int[]{ 0, 0, 0, 0, 0, 0, 0, 0 }
                ),
                new InputPort( -1 )	/* end of table */
        };


        static KEYSet keys[] =
        {
                                        new KEYSet( 1, 2, "MOVE UP" ),
                                        new KEYSet( 1, 3, "MOVE DOWN" ),
                                        new KEYSet( 1, 0, "MOVE LEFT"  ),
                                        new KEYSet( 1, 1, "MOVE RIGHT" ),
                                        new KEYSet( 1, 4, "FIRE" ),
                                        new KEYSet( 1, 5, "MISSILE" ),
                                        new KEYSet( -1 )
        };


        /* This needs a lot of work... */
        static DSW TP84_dsw[] =
        {
                new DSW( 4, 0x03, "LIVES", new String[]{"7", "5", "3", "2" } ),

                new DSW( 4, 0x18, "BONUS AT", new String[]{ "40000 80000", "30000 70000","20000 60000", "10000 50000" } ),
        /*Not sure*/
                new DSW( 4, 0x60, "DIFFICULTY", new String[]{ "HARD", "MEDIUM", "NORMAL", "EASY" }, 1 ),

                new DSW( -1 )
        };

        static TrakPort trak_ports[] =
        {
                new TrakPort( -1 )
        };



        static GfxLayout charlayout = new GfxLayout
        (
                8,8,	/* 8*8 characters */
                1024,	/* 1024 characters */
                2,	/* 2 bits per pixel */
                new int[]{ 4, 0 },	/* the two bitplanes for 4 pixels are packed into one byte */
                new int[]{  0, 1, 2, 3, 8*8+0, 8*8+1, 8*8+2, 8*8+3 },	/* bits are packed in groups of four */
                new int[]{ 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8 },
                16*8	/* every char takes 16 bytes */
        );
        static GfxLayout spritelayout = new GfxLayout
        (
                16,16,	/* 16*16 sprites */
                256,	/* 256 sprites */
                4,	/* 4 bits per pixel */
                new int[]{ 256*64*8+4, 256*64*8+0, 4 ,0 },
                new int[]{ 0, 1, 2, 3, 8*8+0, 8*8+1, 8*8+2, 8*8+3,
                                16*8+0, 16*8+1, 16*8+2, 16*8+3, 24*8+0, 24*8+1, 24*8+2, 24*8+3 },
                new int[]{ 0*8, 1*8, 2*8, 3*8, 4*8, 5*8, 6*8, 7*8,
                                32*8, 33*8, 34*8, 35*8, 36*8, 37*8, 38*8, 39*8 },
                64*8	/* every sprite takes 64 bytes */
        );

        static GfxDecodeInfo gfxdecodeinfo[] =
        {
                new GfxDecodeInfo( 1, 0x0000, charlayout,        0, 64*8 ),
                new GfxDecodeInfo( 1, 0x4000, spritelayout, 64*4*8, 16*8 ),
                new GfxDecodeInfo( -1 ) /* end of array */
        };



        static char color_prom[] =
        {
                /* 2c - palette red component */
                0x00,0x02,0x04,0x06,0x08,0x0A,0x01,0x0F,0x0B,0x05,0x03,0x05,0x07,0x09,0x0B,0x0D,
                0x00,0x02,0x03,0x06,0x0A,0x0C,0x01,0x0F,0x0B,0x05,0x03,0x05,0x07,0x0A,0x0C,0x0D,
                0x00,0x01,0x03,0x05,0x0B,0x0D,0x01,0x0F,0x0B,0x05,0x02,0x04,0x06,0x09,0x0E,0x0D,
                0x00,0x01,0x03,0x05,0x0C,0x0E,0x01,0x0F,0x0B,0x05,0x02,0x04,0x06,0x09,0x0E,0x0D,
                0x00,0x01,0x0A,0x05,0x07,0x0A,0x01,0x0F,0x0B,0x05,0x02,0x0A,0x05,0x07,0x0A,0x0D,
                0x00,0x01,0x0B,0x04,0x06,0x0A,0x01,0x0F,0x0B,0x05,0x02,0x0B,0x04,0x06,0x0A,0x0D,
                0x00,0x02,0x04,0x06,0x08,0x0A,0x01,0x0F,0x0B,0x05,0x03,0x05,0x06,0x08,0x0A,0x0D,
                0x00,0x02,0x04,0x06,0x08,0x0A,0x01,0x0F,0x0B,0x05,0x03,0x05,0x06,0x08,0x0A,0x0D,
                0x00,0x0B,0x09,0x06,0x04,0x04,0x02,0x01,0x09,0x03,0x09,0x06,0x05,0x03,0x0F,0x0D,
                0x00,0x0C,0x0A,0x06,0x04,0x04,0x02,0x01,0x0A,0x03,0x0A,0x06,0x05,0x03,0x0F,0x0D,
                0x00,0x0B,0x0A,0x06,0x04,0x04,0x02,0x01,0x00,0x00,0x0A,0x06,0x05,0x03,0x0F,0x0D,
                0x00,0x09,0x08,0x05,0x03,0x03,0x02,0x01,0x00,0x00,0x09,0x05,0x04,0x03,0x0F,0x0D,
                0x00,0x0C,0x00,0x00,0x07,0x01,0x02,0x06,0x05,0x04,0x06,0x03,0x02,0x02,0x0F,0x0D,
                0x00,0x0F,0x0F,0x00,0x07,0x01,0x01,0x06,0x05,0x04,0x05,0x02,0x02,0x05,0x0F,0x0D,
                0x00,0x0D,0x0B,0x0A,0x0F,0x0A,0x01,0x09,0x09,0x0A,0x0E,0x0C,0x07,0x00,0x00,0x0C,
                0x00,0x0D,0x00,0x00,0x00,0x00,0x00,0x09,0x00,0x0B,0x00,0x01,0x03,0x05,0x07,0x00,
                /* 2d - palette green component */
                0x00,0x04,0x06,0x08,0x0A,0x0C,0x01,0x07,0x03,0x01,0x03,0x05,0x08,0x0A,0x0E,0x0D,
                0x00,0x03,0x05,0x07,0x09,0x0A,0x01,0x07,0x03,0x01,0x03,0x05,0x08,0x0A,0x0C,0x0D,
                0x00,0x02,0x04,0x07,0x09,0x0A,0x01,0x07,0x03,0x01,0x03,0x05,0x07,0x09,0x0A,0x0D,
                0x00,0x02,0x04,0x07,0x08,0x09,0x01,0x07,0x03,0x01,0x03,0x05,0x07,0x08,0x08,0x0D,
                0x00,0x03,0x0A,0x07,0x09,0x0C,0x01,0x07,0x03,0x01,0x02,0x0B,0x06,0x08,0x0D,0x0D,
                0x00,0x02,0x06,0x06,0x08,0x0C,0x01,0x07,0x03,0x01,0x02,0x0C,0x05,0x07,0x0D,0x0D,
                0x00,0x04,0x06,0x08,0x0A,0x0C,0x01,0x07,0x03,0x01,0x03,0x05,0x07,0x09,0x0D,0x0D,
                0x00,0x04,0x06,0x08,0x0A,0x0C,0x01,0x07,0x03,0x01,0x03,0x05,0x07,0x09,0x0D,0x0D,
                0x00,0x09,0x07,0x05,0x03,0x00,0x00,0x00,0x09,0x03,0x09,0x06,0x05,0x03,0x07,0x0D,
                0x00,0x07,0x06,0x04,0x02,0x00,0x00,0x00,0x07,0x01,0x07,0x05,0x03,0x01,0x07,0x0D,
                0x00,0x05,0x04,0x02,0x01,0x01,0x01,0x01,0x0B,0x0B,0x06,0x04,0x02,0x01,0x07,0x0D,
                0x00,0x06,0x04,0x02,0x01,0x01,0x00,0x01,0x0B,0x0B,0x04,0x03,0x02,0x01,0x07,0x0D,
                0x00,0x00,0x00,0x0B,0x0F,0x00,0x00,0x03,0x03,0x03,0x03,0x02,0x01,0x00,0x07,0x0D,
                0x00,0x00,0x0F,0x0C,0x0F,0x00,0x00,0x04,0x04,0x04,0x02,0x01,0x00,0x00,0x07,0x0D,
                0x00,0x0D,0x0B,0x0A,0x0E,0x0A,0x00,0x00,0x00,0x00,0x05,0x0C,0x0E,0x0D,0x09,0x00,
                0x00,0x0D,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x03,0x00,0x00,0x00,0x00,0x00,0x00,
                /* 1e - palette blue component */
                0x00,0x06,0x08,0x0A,0x0C,0x0E,0x01,0x03,0x02,0x01,0x02,0x03,0x04,0x05,0x06,0x0D,
                0x00,0x05,0x07,0x09,0x0A,0x0C,0x01,0x03,0x02,0x01,0x02,0x03,0x04,0x05,0x06,0x0D,
                0x00,0x05,0x06,0x09,0x0A,0x0C,0x01,0x03,0x02,0x01,0x01,0x02,0x04,0x05,0x06,0x0D,
                0x00,0x05,0x06,0x09,0x09,0x0B,0x01,0x03,0x02,0x01,0x01,0x02,0x04,0x04,0x04,0x0D,
                0x00,0x05,0x0D,0x09,0x0B,0x0E,0x01,0x03,0x02,0x01,0x01,0x09,0x03,0x04,0x06,0x0D,
                0x00,0x04,0x0B,0x08,0x0A,0x0E,0x01,0x03,0x02,0x01,0x01,0x0A,0x02,0x03,0x06,0x0D,
                0x00,0x06,0x08,0x0A,0x0C,0x0E,0x01,0x03,0x02,0x01,0x02,0x03,0x04,0x05,0x06,0x0D,
                0x00,0x06,0x08,0x0A,0x0C,0x0E,0x01,0x03,0x02,0x01,0x02,0x03,0x04,0x05,0x06,0x0D,
                0x00,0x00,0x00,0x00,0x00,0x0A,0x07,0x05,0x09,0x03,0x09,0x06,0x05,0x03,0x00,0x0D,
                0x00,0x00,0x00,0x00,0x00,0x09,0x05,0x03,0x05,0x01,0x05,0x04,0x03,0x01,0x00,0x0D,
                0x00,0x00,0x02,0x02,0x01,0x05,0x04,0x03,0x07,0x07,0x06,0x03,0x03,0x02,0x00,0x0D,
                0x00,0x00,0x03,0x04,0x04,0x05,0x04,0x03,0x07,0x07,0x07,0x04,0x03,0x02,0x00,0x0D,
                0x00,0x0F,0x00,0x07,0x0F,0x05,0x04,0x06,0x06,0x06,0x08,0x05,0x04,0x03,0x00,0x0D,
                0x00,0x0F,0x06,0x08,0x0F,0x04,0x03,0x06,0x06,0x06,0x07,0x04,0x03,0x04,0x00,0x0D,
                0x00,0x0D,0x0B,0x0A,0x03,0x03,0x05,0x0D,0x0D,0x00,0x00,0x00,0x00,0x08,0x0D,0x0A,
                0x00,0x0D,0x00,0x00,0x00,0x00,0x00,0x0D,0x00,0x00,0x00,0x04,0x06,0x08,0x0B,0x00,
                /* 1f - character lookup table */
                0x00,0x02,0x03,0x04,0x01,0x02,0x03,0x04,0x05,0x06,0x03,0x04,0x0A,0x0B,0x0D,0x00,
                0x0A,0x0B,0x0D,0x0C,0x00,0x0B,0x0D,0x0C,0x0D,0x0B,0x0D,0x09,0x0A,0x0B,0x0D,0x0E,
                0x0A,0x0C,0x03,0x04,0x0A,0x0B,0x0D,0x08,0x00,0x0B,0x0D,0x09,0x0A,0x0B,0x0C,0x0E,
                0x0A,0x0B,0x0D,0x00,0x0A,0x0B,0x0B,0x0B,0x05,0x06,0x07,0x00,0x0F,0x00,0x00,0x0E,
                0x09,0x05,0x0B,0x06,0x04,0x06,0x00,0x09,0x09,0x01,0x0B,0x06,0x09,0x05,0x02,0x06,
                0x09,0x01,0x02,0x06,0x02,0x09,0x00,0x06,0x09,0x03,0x02,0x06,0x09,0x03,0x00,0x06,
                0x09,0x03,0x00,0x02,0x09,0x05,0x00,0x06,0x0B,0x09,0x00,0x06,0x0C,0x00,0x00,0x0F,
                0x0D,0x04,0x00,0x01,0x0E,0x00,0x00,0x08,0x07,0x00,0x00,0x0A,0x01,0x06,0x00,0x09,
                0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
                0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
                0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,
                0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x0F,0x00,0x00,0x0E,
                0x00,0x0B,0x0C,0x00,0x0A,0x0B,0x0C,0x00,0x05,0x06,0x0C,0x00,0x0A,0x0B,0x0D,0x00,
                0x0A,0x0B,0x0D,0x0C,0x00,0x0B,0x0D,0x0C,0x02,0x0B,0x0D,0x04,0x0A,0x0B,0x0D,0x0E,
                0x0A,0x0C,0x0C,0x00,0x0A,0x0B,0x0D,0x03,0x00,0x0B,0x0D,0x03,0x0A,0x0B,0x0C,0x0E,
                0x0A,0x0B,0x0D,0x01,0x0A,0x09,0x08,0x07,0x05,0x06,0x00,0x00,0x0F,0x00,0x00,0x0E,
                /* 16c - sprite lookup table */
                0x00,0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08,0x09,0x0A,0x0B,0x0C,0x0D,0x0E,0x0F,
                0x00,0x00,0x01,0x02,0x03,0x04,0x06,0x09,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x0E,
                0x00,0x00,0x00,0x01,0x02,0x03,0x06,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x01,
                0x00,0x01,0x02,0x03,0x04,0x05,0x06,0x06,0x06,0x06,0x00,0x00,0x00,0x00,0x0E,0x0F,
                0x00,0x01,0x02,0x03,0x04,0x05,0x06,0x09,0x09,0x06,0x00,0x00,0x0E,0x0F,0x00,0x00,
                0x00,0x01,0x02,0x03,0x04,0x05,0x06,0x08,0x08,0x09,0x0E,0x0F,0x00,0x00,0x00,0x00,
                0x00,0x06,0x09,0x09,0x06,0x0F,0x06,0x07,0x08,0x09,0x0A,0x0B,0x0C,0x0D,0x0E,0x08,
                0x00,0x09,0x06,0x06,0x00,0x08,0x06,0x07,0x08,0x09,0x0A,0x0B,0x0C,0x0D,0x0E,0x00,
                0x00,0x08,0x06,0x06,0x05,0x00,0x06,0x07,0x08,0x09,0x0A,0x0B,0x0C,0x0D,0x0E,0x00,
                0x00,0x07,0x09,0x09,0x00,0x05,0x06,0x07,0x08,0x09,0x0A,0x0B,0x0C,0x0D,0x0E,0x00,
                0x00,0x07,0x08,0x08,0x00,0x00,0x06,0x07,0x08,0x09,0x0A,0x0B,0x0C,0x0D,0x0E,0x05,
                0x00,0x08,0x07,0x07,0x00,0x00,0x06,0x07,0x08,0x09,0x0A,0x0B,0x0C,0x0D,0x0E,0x0A,
                0x00,0x09,0x07,0x07,0x00,0x00,0x06,0x07,0x08,0x09,0x0A,0x0B,0x0C,0x0D,0x0E,0x07,
                0x00,0x06,0x08,0x08,0x00,0x00,0x06,0x07,0x08,0x09,0x0A,0x0B,0x0C,0x0D,0x0E,0x03,
                0x00,0x00,0x00,0x00,0x02,0x03,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x08,
                0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00,0x00
        };



        static MachineDriver machine_driver = new MachineDriver
        (
                /* basic machine hardware  */
                new MachineCPU[]
                {
                    new MachineCPU(
                                CPU_M6809,
                                1500000,			/* ? Mhz 1.1 seem too slow */
                                0,					/* memory region */
                                readmem,			/* MemoryReadAddress */
                                writemem,			/* MemoryWriteAddress */
                                null,					/* IOReadPort */
                                null,					/* IOWritePort */
                                interrupt,			/* interrupt routine */
                                1					/* interrupts per frame */
                        ),
                    new MachineCPU(
                                CPU_M6809,
                                1100000,			/* ? Mhz */
                                2,	/* memory region #2 */
                                readmem_cpu2,
                                writemem_cpu2,
                                null,
                                null,
                                tp84_interrupt,1	/* JB 970829 */ /*256*/
                        ),
                    new MachineCPU(
                                CPU_Z80 | CPU_AUDIO_CPU,
                                3072000,	/* 3.072 Mhz ? */
                                3,	/* memory region #3 */
                                sound_readmem,sound_writemem,null,null,
                                tp84_sh_interrupt,1
                        )
                },
                60,							/* frames per second */
                tp84_init_machine,		/* init machine routine */ /* JB 970829 */

                /* video hardware */
                32*8, 32*8, new rectangle( 0*8, 32*8-1, 2*8, 30*8-1 ),
                gfxdecodeinfo,				/* GfxDecodeInfo * */
                256,4096, /* see tp84_vh_convert_color_prom for explanation */
                tp84_vh_convert_color_prom,							/* convert color prom routine */

                VIDEO_TYPE_RASTER,
                null,							/* vh_init routine */
                tp84_vh_start,			/* vh_start routine */
                tp84_vh_stop,			/* vh_stop routine */
                tp84_vh_screenrefresh,	/* vh_update routine */

                /* sound hardware */
                null,
                null,
                tp84_sh_start,
                tp84_sh_stop,
                tp84_sh_update
        );

        static RomLoadPtr  tp84_rom= new RomLoadPtr(){ public void handler() 
        {
                ROM_REGION(0x10000);	/* 64k for code */
                ROM_LOAD( "tp84_7j.bin",  0x8000, 0x2000, 0xA31F399F );
                ROM_LOAD( "tp84_8j.bin",  0xa000, 0x2000, 0x26F277A6 );
                ROM_LOAD( "tp84_9j.bin",  0xc000, 0x2000, 0x32B33813 );
                ROM_LOAD( "tp84_10j.bin", 0xe000, 0x2000, 0x0EE9E39B );

                ROM_REGION(0xc000);	/* Temporary */
        /*Chars*/
                ROM_LOAD( "tp84_2j.bin", 0x0000, 0x2000, 0x2e1d4fc1 );
                ROM_LOAD( "tp84_1j.bin", 0x2000, 0x2000, 0xa03b6e83 );

        /*Sprites*/
                ROM_LOAD( "tp84_12a.bin", 0x4000, 0x2000, 0xbce053e6 );
                ROM_LOAD( "tp84_13a.bin", 0x6000, 0x2000, 0x6041402f );
                ROM_LOAD( "tp84_14a.bin", 0x8000, 0x2000, 0x766266d0 );
                ROM_LOAD( "tp84_15a.bin", 0xa000, 0x2000, 0x5e4e6bfc );

        /* Second CPU */
                ROM_REGION(0x10000);	/* 64k for the second CPU */
                ROM_LOAD( "tp84_10d.bin", 0xe000, 0x2000, 0x971bf3f1 );

        /* Sound CPU */
                ROM_REGION(0x10000);	/* 64k for code of sound cpu Z80 */
                ROM_LOAD( "tp84s_6a.bin", 0x0000, 0x2000, 0xe2664270 );
                ROM_END();
        }};


        static HiscoreLoadPtr tp84_hiload = new HiscoreLoadPtr() { public int handler()
         {
          char[] RAM = Machine.memory_region[0];
          FILE f;

          /* Wait for hiscore table initialization to be done. */
     /*TOFIX     if (memcmp(RAM,0x57a0,new char[]{0x00,0x02,0x00,0x47,0x53,0x58}, 6) != 0)
            return 0;

          if ((f = fopen(name,"rb")) != null)
            {
              /* Load and set hiscore table. */
/*TOFIX              fread(RAM,0x57a0,1,5*6,f);
              fclose(f);
            }

        /*The Top score seem to be there but it do not work */
        /*  fread(&RAM[0x5736],1,6,f);*/

          return 1;
        }};


        static HiscoreSavePtr tp84_hisave = new HiscoreSavePtr() { public void handler(){
              char[] RAM = Machine.memory_region[0];
          FILE f;

      /*TOFIX    if ((f = fopen(name,"wb")) != null)
            {
              /* Write hiscore table. */
    /*TOFIX          fwrite(RAM,0x57a0,1,5*6,f);
              fclose(f);
            }*/
        /*  fwrite(&RAM[0x5736],1,6,f);*/
        }};



        public static GameDriver tp84_driver =new GameDriver
            (
                "Time Pilot 84",
                "tp84",
                "MARC LAFONTAINE",
                machine_driver,		/* MachineDriver * */

                tp84_rom,			/* RomModule * */
                null, null,					/* ROM decrypt routines */
                null,						/* samplenames */

                TP84_input_ports,	/* InputPort  */
                null,
                trak_ports,                     /* TrackBall  */
                TP84_dsw,		/* DSW        */
                keys,                   /* KEY def    */

                color_prom,						/* color prom */
                null, 	          /* palette */
                null, 	          /* color table */
                ORIENTATION_ROTATE_90,

                tp84_hiload, tp84_hisave
        );    
}
