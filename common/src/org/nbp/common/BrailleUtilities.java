package org.nbp.common;

public abstract class BrailleUtilities {
  public final static char UNICODE_ROW = 0X2800;

  public final static byte DOT_1 =       0X01;
  public final static byte DOT_2 =       0X02;
  public final static byte DOT_3 =       0X04;
  public final static byte DOT_4 =       0X08;
  public final static byte DOT_5 =       0X10;
  public final static byte DOT_6 =       0X20;
  public final static byte DOT_7 =       0X40;
  public final static byte DOT_8 = (byte)0X80;

  private final static int BRF_MINIMUM = 0X20;
  private final static int BRF_MAXIMUM = 0X7E;

  private final static byte[] brfTable = new byte[] {
    /* 20   */ 0,
    /* 21 ! */ DOT_2 | DOT_3 | DOT_4 | DOT_6,
    /* 22 " */ DOT_5,
    /* 23 # */ DOT_3 | DOT_4 | DOT_5 | DOT_6,
    /* 24 $ */ DOT_1 | DOT_2 | DOT_4 | DOT_6,
    /* 25 % */ DOT_1 | DOT_4 | DOT_6,
    /* 26 & */ DOT_1 | DOT_2 | DOT_3 | DOT_4 | DOT_6,
    /* 27 ' */ DOT_3,
    /* 28 ( */ DOT_1 | DOT_2 | DOT_3 | DOT_5 | DOT_6,
    /* 29 ) */ DOT_2 | DOT_3 | DOT_4 | DOT_5 | DOT_6,
    /* 2A * */ DOT_1 | DOT_6,
    /* 2B + */ DOT_3 | DOT_4 | DOT_6,
    /* 2C , */ DOT_6,
    /* 2D - */ DOT_3 | DOT_6,
    /* 2E . */ DOT_4 | DOT_6,
    /* 2F / */ DOT_3 | DOT_4,
    /* 30 0 */ DOT_3 | DOT_5 | DOT_6,
    /* 31 1 */ DOT_2,
    /* 32 2 */ DOT_2 | DOT_3,
    /* 33 3 */ DOT_2 | DOT_5,
    /* 34 4 */ DOT_2 | DOT_5 | DOT_6,
    /* 35 5 */ DOT_2 | DOT_6,
    /* 36 6 */ DOT_2 | DOT_3 | DOT_5,
    /* 37 7 */ DOT_2 | DOT_3 | DOT_5 | DOT_6,
    /* 38 8 */ DOT_2 | DOT_3 | DOT_6,
    /* 39 9 */ DOT_3 | DOT_5,
    /* 3A : */ DOT_1 | DOT_5 | DOT_6,
    /* 3B ; */ DOT_5 | DOT_6,
    /* 3C < */ DOT_1 | DOT_2 | DOT_6,
    /* 3D = */ DOT_1 | DOT_2 | DOT_3 | DOT_4 | DOT_5 | DOT_6,
    /* 3E > */ DOT_3 | DOT_4 | DOT_5,
    /* 3F ? */ DOT_1 | DOT_4 | DOT_5 | DOT_6,
    /* 40 ~ */ DOT_4,
    /* 41 A */ DOT_1,
    /* 42 B */ DOT_1 | DOT_2,
    /* 43 C */ DOT_1 | DOT_4,
    /* 44 D */ DOT_1 | DOT_4 | DOT_5,
    /* 45 E */ DOT_1 | DOT_5,
    /* 46 F */ DOT_1 | DOT_2 | DOT_4,
    /* 47 G */ DOT_1 | DOT_2 | DOT_4 | DOT_5,
    /* 48 H */ DOT_1 | DOT_2 | DOT_5,
    /* 49 I */ DOT_2 | DOT_4,
    /* 4A J */ DOT_2 | DOT_4 | DOT_5,
    /* 4B K */ DOT_1 | DOT_3,
    /* 4C L */ DOT_1 | DOT_2 | DOT_3,
    /* 4D M */ DOT_1 | DOT_3 | DOT_4,
    /* 4E N */ DOT_1 | DOT_3 | DOT_4 | DOT_5,
    /* 4F O */ DOT_1 | DOT_3 | DOT_5,
    /* 50 P */ DOT_1 | DOT_2 | DOT_3 | DOT_4,
    /* 51 Q */ DOT_1 | DOT_2 | DOT_3 | DOT_4 | DOT_5,
    /* 52 R */ DOT_1 | DOT_2 | DOT_3 | DOT_5,
    /* 53 S */ DOT_2 | DOT_3 | DOT_4,
    /* 54 T */ DOT_2 | DOT_3 | DOT_4 | DOT_5,
    /* 55 U */ DOT_1 | DOT_3 | DOT_6,
    /* 56 V */ DOT_1 | DOT_2 | DOT_3 | DOT_6,
    /* 57 W */ DOT_2 | DOT_4 | DOT_5 | DOT_6,
    /* 58 X */ DOT_1 | DOT_3 | DOT_4 | DOT_6,
    /* 59 Y */ DOT_1 | DOT_3 | DOT_4 | DOT_5 | DOT_6,
    /* 5A Z */ DOT_1 | DOT_3 | DOT_5 | DOT_6,
    /* 5B [ */ DOT_2 | DOT_4 | DOT_6,
    /* 5C \ */ DOT_1 | DOT_2 | DOT_5 | DOT_6,
    /* 5D ] */ DOT_1 | DOT_2 | DOT_4 | DOT_5 | DOT_6,
    /* 5E ^ */ DOT_4 | DOT_5,
    /* 5F _ */ DOT_4 | DOT_5 | DOT_6
  };

  private final static int brfToIndex (byte brf) {
    if ((brf < BRF_MINIMUM) || (brf > BRF_MAXIMUM)) return -1;
    if ((brf & 0X40) != 0) brf &= 0X5F;
    return brf - BRF_MINIMUM;
  }

  public final static char brfToCharacter (byte brf) {
    int index = brfToIndex(brf);
    if (index < 0) return 0;

    char character = UNICODE_ROW;
    character |= brfTable[index] & 0XFF;
    return character;
  }

  private BrailleUtilities () {
  }
}
