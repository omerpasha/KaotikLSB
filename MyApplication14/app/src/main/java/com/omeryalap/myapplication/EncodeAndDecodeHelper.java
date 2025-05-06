package com.omeryalap.myapplication;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Environment;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class EncodeAndDecodeHelper {
    private static final double a = 3.6;
    private static final double b = 0.4;
    // private static final int iterations = 10000;
    static ArrayList<Integer> siralar = new ArrayList<>();
    static double x0 = Constants.CHAOTIC_MAP_INITIAL_VALUE;

    public static int siraGetir() {
        x0 = chaoticMap(x0);
        int arasira = ((int) (x0 * 10000000000000000.)) % siralar.size();
        int sira = siralar.get(arasira);
        siralar.remove(arasira);
        return sira;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("SuspiciousIndentation")
    public static Bitmap Encode(Bitmap bitmap, String textToHide) {
        long startTime = System.nanoTime();
        // String text = "AB";
        int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];

        for (int i = 0; i < pixels.length; i++)
            siralar.add(i);

        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        int sira = 0;
        int pixel = 0;
        // Extract the color channels
        int alpha = 0;
        int red = 0;
        int green = 0;
        int blue = 0;


        //burada 9 bite tamamlıyoruz
        sira = siraGetir();
        for (char c : textToHide.toCharArray()) {
            String binary = Integer.toBinaryString(c);
            while (binary.length() < 9) {
                binary = "0" + binary; // Ensure it's bits
            }
            // ANAHTAR


            //Pixel sırası hesaplama
            for (int i = 0; i < 9; i++) {
                if (i % 3 == 0) {

                    pixel = pixels[sira];
                    // Extract the color channels
                    alpha = Color.alpha(pixel);
                    red = Color.red(pixel);
                    green = Color.green(pixel);
                    blue = Color.blue(pixel);

                }

                switch (i % 3) {
                    case 0:
                        red = mygetPixel(red, binary, i);
                        break;
                    case 1:
                        green = mygetPixel(green, binary, i);
                        break;
                    case 2:
                        blue = mygetPixel(blue, binary, i);
                        break;

                }

                // sıradaki pixeli almak için metodumuz
                // parametre olarak pixel sırası gönderiyoruz.
                if (i % 3 == 2) {
                    int newColor = Color.argb(alpha, red, green, blue);
                    //int blue =Color.blue(pixels[2])
                    //burada br sıkıntı var
                    pixels[sira] = newColor;
                    sira = siraGetir();
                }


            } // for  her bir karakterdeki bit adedi kadar 9

        }// for Tüm gizlenecek karakterler için

        alpha = 255;//-1 olması için hepsi 255
        red = 255;
        green = 255;
        blue = 255;
        //siradaki 9 bitlik  3 pixeli al ve -1 değeri yaz bu flag olacak decod ederken
        int newColor = Color.argb(alpha, red, green, blue);
        sira = siraGetir();
        pixels[sira] = newColor;


        Bitmap bitmap2 = Bitmap.createBitmap(pixels, bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);

        bitmap2.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        // Kullanım örneği
        Bitmap yourBitmap = bitmap2; // Kaydetmek istediğiniz Bitmap nesnesi
        String fileName = Constants.ENCODED_IMAGE_FILENAME; // Dosya adı
        saveBitmapToStorage(yourBitmap, fileName);
        long endTime = System.nanoTime();
        System.out.println("Encode süresi: " + (endTime - startTime) / 1_000_000.0 + " ms");
        return yourBitmap;
    }

    private static int mygetPixel(int pixel, String binary, int i) {
        //TODO  11100101 YAZMAK İSTEDİĞİMİZ
        //      01000001 65
        //
        if (binary.charAt(i) == '1') {
            if (pixel % 2 == 0) {
                pixel += 1;
            }
        } else {
            if (pixel % 2 == 1) {
                pixel -= 1;
            }
        }
        return pixel;

    }

    public static String Decode(Bitmap bitmap) {
        long startTime = System.nanoTime();
        int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];
        x0 = Constants.CHAOTIC_MAP_INITIAL_VALUE;
        siralar.clear();
        for (int i = 0; i < pixels.length; i++)
            siralar.add(i);
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        // int green =Color.green(pixels[6]) //29 olarak dopru değeri döndürür
        int sira = 0;
        // int pixel = pixels[sira];

        int red = 0;
        int green = 0;
        int blue = 0;


        String binary = "";

        sira = siraGetir();
        //Pixel sırası hesaplama
        for (int i = 0; i < bitmap.getWidth() * bitmap.getHeight() * 9; i++) {//burada kaç karakter varsa 9la çarp

            if (Color.red(pixels[sira]) == 255 && Color.green(pixels[sira]) == 255 && Color.blue(pixels[sira]) == 255 && Color.alpha(pixels[sira]) == 255) {
                //decode ederken -1 değeri gelirse direk döngüden çıkmak için
                binary.concat("11111111");//-1 değeri binary karşılığı için 8 adet 1 eklendi çünkü aşağıda 9 lu gruplara ayırıyoruz
                String result = binarySplitter(binary);
                long endTime = System.nanoTime();
                System.out.println("Decode süresi: " + (endTime - startTime) / 1_000_000.0 + " ms");
                return result;
            }


            switch (i % 3) {
                case 0:
                    red = Color.red(pixels[sira]);
                    binary = mygetPixel2(red, binary, i);
                    break;
                case 1:
                    green = Color.green(pixels[sira]);
                    binary = mygetPixel2(green, binary, i);
                    break;
                case 2:
                    blue = Color.blue(pixels[sira]);
                    binary = mygetPixel2(blue, binary, i);
                    break;

            }
            if (i % 3 == 2) {
                sira = siraGetir();
            }
        }
        return binarySplitter(binary);
    }

    private static String mygetPixel2(int pixel, String binary, int i) {

        int snc = pixel & 1;
        if (snc == 1) {
            binary = binary + "1";
        } else {
            binary = binary + "0";
        }


        return binary;

    }

    public static void saveBitmapToStorage(Bitmap bitmap, String filename) {
        File directory = new File(Environment.getExternalStorageDirectory() + File.separator + Constants.STEGANOGRAPHY_FOLDER);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File file = new File(directory, filename);

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String binarySplitter(String binaryNumber) {
        StringBuilder binarySplitting = new StringBuilder();
        // 9 bitlik gruplara ayırmak için bir döngü kullan
        int groupSize = Constants.BINARY_GROUP_SIZE;
        int length = binaryNumber.length();
        int numGroups = (int) Math.ceil((double) length / groupSize);
        String[] bitGroups = new String[numGroups];

        for (int i = 0; i < numGroups; i++) {
            int startIndex = i * groupSize;
            int endIndex = Math.min(startIndex + groupSize, length);
            bitGroups[i] = binaryNumber.substring(startIndex, endIndex);
        }

        // Her grup dizi içinde depolanır
        for (String group : bitGroups) {
            if (!group.equals("1"))
                binarySplitting.append(binaryToChar(group));
        }
        return binarySplitting.toString();
    }

    public static String binaryToChar(String binary) {
        String extractedText = "";
        int charCode = Integer.parseInt(binary, 2);                //her karakter için
        extractedText = new Character((char) charCode).toString();      //oluştur
        return extractedText;
    }

    private static double chaoticMap(double x) {
        //logistic map
        double lambda = Constants.CHAOTIC_MAP_LAMBDA;
        return lambda * x * (1 - x);
    }
}
