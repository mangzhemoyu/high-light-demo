package org.example;

import org.apache.poi.sl.usermodel.ColorStyle;
import org.apache.poi.sl.usermodel.HighlightColorSupport;
import org.apache.poi.ss.usermodel.Color;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTHighlight;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STHighlightColor;

import java.io.FileInputStream;
import java.io.FileOutputStream;

public class WordHighlighter {

    public static void main(String[] args) {
        String filePath = "/Users/xiongyunfei/Downloads/demo.docx";
        String targetText = "根据知识库中的资料，Java处理不同文件类型的高亮需要不同的库。例如，对于Word文档，可以使用Spire.Doc或Apache POI";

        try (XWPFDocument doc = new XWPFDocument(new FileInputStream(filePath))) {
            for (XWPFParagraph p : doc.getParagraphs()) {
                for (XWPFRun run : p.getRuns()) {
                    String text = run.getText(0);
                    if (text != null && text.contains(targetText)) {
                        // 设置背景色为黄色高亮
                        run.getCTR().addNewRPr().addNewShd().setFill("FFFF00"); // 黄色填充
//                        run.setHighlightColor(HighlightColor.YELLOW);
                    }
                }
            }
            doc.write(new FileOutputStream("/Users/xiongyunfei/Downloads/output.docx"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
