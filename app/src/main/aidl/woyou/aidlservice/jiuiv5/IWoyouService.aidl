// IWoyouService.aidl
// Standard Sunmi built-in printer AIDL interface (package name is fixed by Sunmi
// across all their devices — do not rename this file or its package).
package woyou.aidlservice.jiuiv5;

import woyou.aidlservice.jiuiv5.ICallback;

interface IWoyouService {

    void printerInit(ICallback callback);

    void printerSelfChecking(ICallback callback);

    String getPrinterSerialNo();

    String getPrinterVersion();

    String getPrinterModal();

    String getPrinterFactory();

    int updatePrinterState();

    int getPrinterHead();

    float getPrinterPaper();

    int getPrinterHeadState();

    void sendRAWData(in byte[] data, ICallback callback);

    void setPrinterStyle(int key, int value);

    void printText(String text, ICallback callback);

    void printTextWithFont(String text, String typeface, float fontsize, ICallback callback);

    void printSpecFormatText(String text, String typeface, float fontsize, int alignment, ICallback callback);

    void printOriginalText(String text, ICallback callback);

    void printColumnsText(in String[] colsTextArr, in int[] colsWidthArr, in int[] colsAlign, ICallback callback);

    void printColumnsString(in String[] colsTextArr, in int[] colsWidthArr, in int[] colsAlign, ICallback callback);

    void printBitmap(in Bitmap bitmap, ICallback callback);

    void printBarCode(String data, int symbology, int height, int width, int textposition, ICallback callback);

    void printQRCode(String data, int modulesize, int errorlevel, ICallback callback);

    void print2DCode(String data, int symbology, int modulesize, int errorlevel, ICallback callback);

    void commitPrinterBuffer();

    void enterPrinterBuffer(boolean clean);

    void exitPrinterBuffer(boolean commit);

    void lineWrap(int lines, ICallback callback);

    void sendRAWData2(String base64Data, ICallback callback);

    void printTextBitmap(String text, in Bundle typeface, ICallback callback);

    void setAlgorithmBmp(int algorithm, ICallback callback);

    boolean isPrinterOnLine();

    boolean isPrinterReady();

    void printBitmapCustom(in Bitmap bitmap, int pictureStyle, ICallback callback);

    void cutpaper(ICallback callback);

    void setFontZoom(int widthzoom, int heightzoom, ICallback callback);

    void setFontWeight(boolean fontweight, ICallback callback);

    void printTable(in String[][] jsonData, ICallback callback);

    void feedPaper(ICallback callback);

    void printQRCodeCustom(String data, int modulesize, int errorlevel, ICallback callback);

    void printBarCodeCustom(String data, int symbology, int height, int width, int textposition, ICallback callback);

    void restoreDefaultLineSpacing(ICallback callback);

    void sendCPCLData(String data, ICallback callback);

    void sendUserCmd(in byte[] data, ICallback callback);
}
