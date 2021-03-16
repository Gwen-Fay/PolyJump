package main;

import org.lwjgl.input.Keyboard;

/**
 * Created by gwen on 7/19/16.
 */
public class KeyInput {

    public static char getChar(int code, boolean isShift) {
        if (!isShift) {
            switch (code) {
                case Keyboard.KEY_GRAVE:
                    return '`';
                case Keyboard.KEY_0:
                    return '0';
                case Keyboard.KEY_1:
                    return '1';
                case Keyboard.KEY_2:
                    return '2';
                case Keyboard.KEY_3:
                    return '3';
                case Keyboard.KEY_4:
                    return '4';
                case Keyboard.KEY_5:
                    return '5';
                case Keyboard.KEY_6:
                    return '6';
                case Keyboard.KEY_7:
                    return '7';
                case Keyboard.KEY_8:
                    return '8';
                case Keyboard.KEY_9:
                    return '9';
                case Keyboard.KEY_MINUS:
                    return '-';
                case Keyboard.KEY_EQUALS:
                    return '=';

                case Keyboard.KEY_Q:
                    return 'q';
                case Keyboard.KEY_W:
                    return 'w';
                case Keyboard.KEY_E:
                    return 'e';
                case Keyboard.KEY_R:
                    return 'r';
                case Keyboard.KEY_T:
                    return 't';
                case Keyboard.KEY_Y:
                    return 'y';
                case Keyboard.KEY_U:
                    return 'u';
                case Keyboard.KEY_I:
                    return 'i';
                case Keyboard.KEY_O:
                    return 'o';
                case Keyboard.KEY_P:
                    return 'p';
                case Keyboard.KEY_LBRACKET:
                    return '[';
                case Keyboard.KEY_RBRACKET:
                    return ']';
                case Keyboard.KEY_BACKSLASH:
                    return '\\';

                case Keyboard.KEY_A:
                    return 'a';
                case Keyboard.KEY_S:
                    return 's';
                case Keyboard.KEY_D:
                    return 'd';
                case Keyboard.KEY_F:
                    return 'f';
                case Keyboard.KEY_G:
                    return 'g';
                case Keyboard.KEY_H:
                    return 'h';
                case Keyboard.KEY_J:
                    return 'j';
                case Keyboard.KEY_K:
                    return 'k';
                case Keyboard.KEY_L:
                    return 'l';
                case Keyboard.KEY_SEMICOLON:
                    return ';';
                case Keyboard.KEY_APOSTROPHE:
                    return '\'';

                case Keyboard.KEY_Z:
                    return 'z';
                case Keyboard.KEY_X:
                    return 'x';
                case Keyboard.KEY_C:
                    return 'c';
                case Keyboard.KEY_V:
                    return 'v';
                case Keyboard.KEY_B:
                    return 'b';
                case Keyboard.KEY_N:
                    return 'n';
                case Keyboard.KEY_M:
                    return 'm';
                case Keyboard.KEY_COMMA:
                    return ',';
                case Keyboard.KEY_PERIOD:
                    return '.';
                case Keyboard.KEY_SLASH:
                    return '/';

                case Keyboard.KEY_SPACE:
                    return ' ';
                default:
                    return '\u0000';
            }
        } else {
            switch (code) {
                case Keyboard.KEY_GRAVE:
                    return '~';
                case Keyboard.KEY_0:
                    return ')';
                case Keyboard.KEY_1:
                    return '!';
                case Keyboard.KEY_2:
                    return '@';
                case Keyboard.KEY_3:
                    return '#';
                case Keyboard.KEY_4:
                    return '$';
                case Keyboard.KEY_5:
                    return '%';
                case Keyboard.KEY_6:
                    return '^';
                case Keyboard.KEY_7:
                    return '&';
                case Keyboard.KEY_8:
                    return '*';
                case Keyboard.KEY_9:
                    return '(';
                case Keyboard.KEY_MINUS:
                    return '_';
                case Keyboard.KEY_EQUALS:
                    return '+';

                case Keyboard.KEY_Q:
                    return 'Q';
                case Keyboard.KEY_W:
                    return 'W';
                case Keyboard.KEY_E:
                    return 'E';
                case Keyboard.KEY_R:
                    return 'R';
                case Keyboard.KEY_T:
                    return 'T';
                case Keyboard.KEY_Y:
                    return 'Y';
                case Keyboard.KEY_U:
                    return 'U';
                case Keyboard.KEY_I:
                    return 'I';
                case Keyboard.KEY_O:
                    return 'O';
                case Keyboard.KEY_P:
                    return 'P';
                case Keyboard.KEY_LBRACKET:
                    return '{';
                case Keyboard.KEY_RBRACKET:
                    return '}';
                case Keyboard.KEY_BACKSLASH:
                    return '|';

                case Keyboard.KEY_A:
                    return 'A';
                case Keyboard.KEY_S:
                    return 'S';
                case Keyboard.KEY_D:
                    return 'D';
                case Keyboard.KEY_F:
                    return 'F';
                case Keyboard.KEY_G:
                    return 'G';
                case Keyboard.KEY_H:
                    return 'H';
                case Keyboard.KEY_J:
                    return 'J';
                case Keyboard.KEY_K:
                    return 'K';
                case Keyboard.KEY_L:
                    return 'L';
                case Keyboard.KEY_COLON:
                    return ':';
                case Keyboard.KEY_APOSTROPHE:
                    return '"';

                case Keyboard.KEY_Z:
                    return 'Z';
                case Keyboard.KEY_X:
                    return 'X';
                case Keyboard.KEY_C:
                    return 'C';
                case Keyboard.KEY_V:
                    return 'V';
                case Keyboard.KEY_B:
                    return 'B';
                case Keyboard.KEY_N:
                    return 'N';
                case Keyboard.KEY_M:
                    return 'M';
                case Keyboard.KEY_COMMA:
                    return '<';
                case Keyboard.KEY_PERIOD:
                    return '>';
                case Keyboard.KEY_SLASH:
                    return '?';

                case Keyboard.KEY_SPACE:
                    return ' ';
                default:
                    return '\u0000';
            }
        }
    }
}
