package winapi.components;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class WinKeyTest {

    @Test
    public void mapIntToWinkeys() throws Exception {
        List<WinKey> winKeys = WinKey.mapIntToWinkeys(156701);
        Assert.assertEquals(WinKey.D0, winKeys.get(4));
        Assert.assertEquals(WinKey.D6, winKeys.get(2));
    }

    @Test
    public void mapStringToWinkeys() throws Exception {
        List<WinKey> winKeys = WinKey.mapStringToWinkeys("fAgZ1z3");
        Assert.assertEquals(WinKey.f, winKeys.get(0));
        Assert.assertEquals(WinKey.D3, winKeys.get(6));
    }

    @Test
    public void charToWinKeyDigits() throws Exception {
        Assert.assertEquals(WinKey.D0, WinKey.charToWinKey('0'));
        Assert.assertEquals(WinKey.D1, WinKey.charToWinKey('1'));
        Assert.assertEquals(WinKey.D2, WinKey.charToWinKey('2'));
        Assert.assertEquals(WinKey.D3, WinKey.charToWinKey('3'));
        Assert.assertEquals(WinKey.D4, WinKey.charToWinKey('4'));
        Assert.assertEquals(WinKey.D5, WinKey.charToWinKey('5'));
        Assert.assertEquals(WinKey.D6, WinKey.charToWinKey('6'));
        Assert.assertEquals(WinKey.D7, WinKey.charToWinKey('7'));
        Assert.assertEquals(WinKey.D8, WinKey.charToWinKey('8'));
        Assert.assertEquals(WinKey.D9, WinKey.charToWinKey('9'));
    }

    @Test(expected = IllegalArgumentException.class)
    public void charToWinKeyIllegalException() {
        WinKey.charToWinKey('+');
    }

    @Test
    public void charToWinKey() throws Exception {
        Assert.assertEquals(WinKey.A, WinKey.charToWinKey('A'));
        Assert.assertEquals(WinKey.B, WinKey.charToWinKey('B'));
        Assert.assertEquals(WinKey.C, WinKey.charToWinKey('C'));
        Assert.assertEquals(WinKey.D, WinKey.charToWinKey('D'));
        Assert.assertEquals(WinKey.E, WinKey.charToWinKey('E'));
        Assert.assertEquals(WinKey.F, WinKey.charToWinKey('F'));
        Assert.assertEquals(WinKey.G, WinKey.charToWinKey('G'));
        Assert.assertEquals(WinKey.H, WinKey.charToWinKey('H'));
        Assert.assertEquals(WinKey.I, WinKey.charToWinKey('I'));
        Assert.assertEquals(WinKey.J, WinKey.charToWinKey('J'));
        Assert.assertEquals(WinKey.K, WinKey.charToWinKey('K'));
        Assert.assertEquals(WinKey.L, WinKey.charToWinKey('L'));
        Assert.assertEquals(WinKey.M, WinKey.charToWinKey('M'));
        Assert.assertEquals(WinKey.N, WinKey.charToWinKey('N'));
        Assert.assertEquals(WinKey.O, WinKey.charToWinKey('O'));
        Assert.assertEquals(WinKey.P, WinKey.charToWinKey('P'));
        Assert.assertEquals(WinKey.Q, WinKey.charToWinKey('Q'));
        Assert.assertEquals(WinKey.R, WinKey.charToWinKey('R'));
        Assert.assertEquals(WinKey.S, WinKey.charToWinKey('S'));
        Assert.assertEquals(WinKey.T, WinKey.charToWinKey('T'));
        Assert.assertEquals(WinKey.U, WinKey.charToWinKey('U'));
        Assert.assertEquals(WinKey.V, WinKey.charToWinKey('V'));
        Assert.assertEquals(WinKey.W, WinKey.charToWinKey('W'));
        Assert.assertEquals(WinKey.X, WinKey.charToWinKey('X'));
        Assert.assertEquals(WinKey.Y, WinKey.charToWinKey('Y'));
        Assert.assertEquals(WinKey.Z, WinKey.charToWinKey('Z'));

        Assert.assertEquals(WinKey.a, WinKey.charToWinKey('a'));
        Assert.assertEquals(WinKey.b, WinKey.charToWinKey('b'));
        Assert.assertEquals(WinKey.c, WinKey.charToWinKey('c'));
        Assert.assertEquals(WinKey.d, WinKey.charToWinKey('d'));
        Assert.assertEquals(WinKey.e, WinKey.charToWinKey('e'));
        Assert.assertEquals(WinKey.f, WinKey.charToWinKey('f'));
        Assert.assertEquals(WinKey.g, WinKey.charToWinKey('g'));
        Assert.assertEquals(WinKey.h, WinKey.charToWinKey('h'));
        Assert.assertEquals(WinKey.i, WinKey.charToWinKey('i'));
        Assert.assertEquals(WinKey.j, WinKey.charToWinKey('j'));
        Assert.assertEquals(WinKey.k, WinKey.charToWinKey('k'));
        Assert.assertEquals(WinKey.l, WinKey.charToWinKey('l'));
        Assert.assertEquals(WinKey.m, WinKey.charToWinKey('m'));
        Assert.assertEquals(WinKey.n, WinKey.charToWinKey('n'));
        Assert.assertEquals(WinKey.o, WinKey.charToWinKey('o'));
        Assert.assertEquals(WinKey.p, WinKey.charToWinKey('p'));
        Assert.assertEquals(WinKey.q, WinKey.charToWinKey('q'));
        Assert.assertEquals(WinKey.r, WinKey.charToWinKey('r'));
        Assert.assertEquals(WinKey.s, WinKey.charToWinKey('s'));
        Assert.assertEquals(WinKey.t, WinKey.charToWinKey('t'));
        Assert.assertEquals(WinKey.u, WinKey.charToWinKey('u'));
        Assert.assertEquals(WinKey.v, WinKey.charToWinKey('v'));
        Assert.assertEquals(WinKey.w, WinKey.charToWinKey('w'));
        Assert.assertEquals(WinKey.x, WinKey.charToWinKey('x'));
        Assert.assertEquals(WinKey.y, WinKey.charToWinKey('y'));
        Assert.assertEquals(WinKey.z, WinKey.charToWinKey('z'));
    }
}