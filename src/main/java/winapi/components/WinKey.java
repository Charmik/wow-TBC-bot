package winapi.components;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.sun.jna.platform.win32.WinDef.WPARAM;

public enum WinKey {
    D0(48),
    D1(49),
    D2(50),
    D3(51),
    D4(52),
    D5(53),
    D6(54),
    D7(55),
    D8(56),
    D9(57),
    MINUS(189),
    PLUS(187),
    A(65),
    B(66),
    C(67),
    D(68),
    E(69),
    F(70),
    G(71),
    H(72),
    I(73),
    J(74),
    K(75),
    L(76),
    M(77),
    N(78),
    O(79),
    P(80),
    Q(81),
    R(82),
    S(83),
    T(84),
    U(85),
    V(86),
    W(87),
    X(88),
    Y(89),
    Z(90),
    a(97),
    b(98),
    c(99),
    d(100),
    e(101),
    f(102),
    g(103),
    h(104),
    i(105),
    j(106),
    k(107),
    l(108),
    m(109),
    n(110),
    o(111),
    p(112),
    q(113),
    r(114),
    s(115),
    t(116),
    u(117),
    v(118),
    w(119),
    x(120),
    y(121),
    z(122),
    MOUSE_LEFT(1),
    MOUSE_RIGHT(2),
    MOUSE_MIDDLE(4),
    CTRL(17),
    CTRL_LEFT(162),
    CTRL_RIGHT(163),
    ALT(18),
    ALT_LEFT(164),
    ALT_RIGHT(165),
    SHIFT(16),
    SHIFT_LEFT(160),
    SHIFT_RIGHT(161),
    CAPS_LOCK(20),
    BACKSPACE(8),
    COMMA(44),
    ENTER(13),
    ESC(27),
    SPACEBAR(32),
    TAB(9),
    NONE(0),
    ARROW_LEFT(37),
    ARROW_UP(38),
    ARROW_RIGHT(39),
    LEFT_BRACKET(40),
    RIGHT_BRACKET(41),
    F1(112),
    F2(113),
    F3(114),
    F4(115),
    F5(116),
    F6(117),
    F7(118),
    F8(119),
    F9(120),
    F10(121),
    F11(122),
    F12(123),
    NUM_LOCK(144),
    NUMPAD_0(96),
    NUMPAD_1(97),
    NUMPAD_2(98),
    NUMPAD_3(99),
    NUMPAD_4(100),
    NUMPAD_5(101),
    NUMPAD_6(102),
    NUMPAD_7(103),
    NUMPAD_8(104),
    NUMPAD_9(105),

    SLASH(47),
    DOUBLE_QUOTES(34);

    public static final WinKey[] keys = new WinKey[255];

    static {
        Arrays.stream(values()).forEach((key) -> {
            keys[key.value] = key;
        });
    }

    private int value;
    private WPARAM wparam;

    WinKey(int value) {
        this.value = value;
    }

    public WPARAM getWParam() {
        if (this.wparam == null) {
            this.wparam = new WPARAM((long) this.value);
        }

        return this.wparam;
    }

    public static WinKey charToWinKey(char fieldName) {
        Class refEnum = WinKey.A.getClass();
        try {
            Field key;
            if (Character.isDigit(fieldName)) {
                key = refEnum.getDeclaredField("D" + Character.toString(fieldName));
            } else {
                key = refEnum.getDeclaredField(Character.toString(fieldName));
            }
            try {
                return (WinKey) key.get(refEnum);
            } catch (IllegalAccessException e) {
                throw new ClassCastException("the field is not in a WinKey");
            }
        } catch (NoSuchFieldException e) {
            throw new IllegalArgumentException("Illegal field name: " + fieldName);
        }
    }

    public static List<WinKey> mapStringToWinkeys(String str) {
        ArrayList<WinKey> winKeys = new ArrayList<>(str.length());
        for (int i = 0; i < str.length(); i++) {
            winKeys.add(charToWinKey(str.charAt(i)));
        }
        return winKeys;
    }

    public static List<WinKey> mapIntToWinkeys(int number) {
        ArrayList<WinKey> winKeys = new ArrayList<>();
        if (number == 0) {
            winKeys.add(charToWinKey('0'));
        }
        while (number > 0) {
            winKeys.add(charToWinKey(Character.forDigit(number % 10, 10)));
            number /= 10;
        }
        Collections.reverse(winKeys);
        return winKeys;
    }
}
