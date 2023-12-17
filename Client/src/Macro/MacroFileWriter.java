package Macro;

import java.io.BufferedWriter;
import java.io.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class MacroFileWriter {
    public static void writeMacroToFile(String folderPath, String fileName, Map<String, String> macro) {
        File textFolder = new File(folderPath);

        // 폴더가 존재하지 않으면 생성
        if (!textFolder.exists()) {
            textFolder.mkdirs();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(textFolder, fileName)))) {
            // 매크로 데이터를 파일에 쓰기
            for (Map.Entry<String, String> entry : macro.entrySet()) {
                writer.write(entry.getKey() + ": " + entry.getValue() + "\n");
            }

            System.out.println("Macro data written to: " + textFolder.getAbsolutePath() + File.separator + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

