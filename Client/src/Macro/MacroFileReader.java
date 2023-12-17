package Macro;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class MacroFileReader {
    public static void readMacroFile(String filePath, Map<String, String> macro) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isEmpty = true;

            while ((line = reader.readLine()) != null) {
                // ":"을 기준으로 키와 값을 나눕니다.
                String[] parts = line.split(": ", 2);

                // 라인이 예상 형식과 일치하는지 확인합니다.
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String value = parts[1].trim();
                    macro.put(key, value);
                    isEmpty = false; // 파일이 비어 있지 않음을 표시
                } else {
                    // 예상 형식과 일치하지 않는 라인을 처리합니다.
                    System.err.println("잘못된 라인 형식: " + line);
                }
            }

            if (isEmpty) {
                System.out.println("You don't have a macro in: " + filePath);
            } else {
                System.out.println("매크로 데이터를 읽어왔습니다: " + filePath);
            }
        } catch (IOException e) {
            System.out.println("You don't have a macro file : " + filePath);
        }
    }
}