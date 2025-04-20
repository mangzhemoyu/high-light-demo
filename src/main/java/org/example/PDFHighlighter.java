//package org.example;
//
//
//import org.apache.pdfbox.Loader;
//import org.apache.pdfbox.pdmodel.PDDocument;
//import org.apache.pdfbox.pdmodel.PDPage;
//import org.apache.pdfbox.pdmodel.common.PDRectangle;
//import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
//import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
//import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationHighlight;
//import org.apache.pdfbox.text.PDFTextStripper;
//import org.apache.pdfbox.text.TextPosition;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
//public class PDFHighlighter {
//    static String inputPath = "/Users/xiongyunfei/Downloads/input.pdf";
//    static String outputPath = "/Users/xiongyunfei/Downloads/output.pdf";
//    static String searchText = "Apache";
//
//    public static void main(String[] args) {
//        try (PDDocument document = Loader.loadPDF(new File(inputPath))) {
//            // 遍历每一页
//            for (int pageIndex = 0; pageIndex < document.getNumberOfPages(); pageIndex++) {
//                PDPage page = document.getPage(pageIndex);
//
//                // 提取文本及位置信息
//                TextPositionExtractor stripper = new TextPositionExtractor();
//                stripper.setStartPage(pageIndex + 1);
//                stripper.setEndPage(pageIndex + 1);
//                String pageText = stripper.getText(document);
//                List<TextPosition> textPositions = stripper.getTextPositions();
//
//                int index = 0;
//                while (index < pageText.length()) {
//                    // 查找目标文本
//                    int foundIndex = pageText.indexOf(searchText, index);
//                    if (foundIndex == -1) break;
//
//                    int endIndex = foundIndex + searchText.length();
//                    if (endIndex > textPositions.size()) break;
//
//                    // 获取匹配的文本位置
//                    List<TextPosition> matched = textPositions.subList(foundIndex, endIndex);
//                    if (!matched.isEmpty()) {
//                        // 创建高亮注释并添加到页面
//                        PDAnnotationHighlight highlight = createHighlight(matched);
//                        page.getAnnotations().add(highlight);
//                    }
//                    index = endIndex;
//                }
//            }
//            // 保存修改后的文档
//            document.save(outputPath);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    // 创建高亮注释
//    private static PDAnnotationHighlight createHighlight(List<TextPosition> positions) {
//        TextPosition first = positions.get(0);
//        TextPosition last = positions.get(positions.size() - 1);
//
//        // 计算边界框
//        float llx = first.getXDirAdj();
//        float lly = first.getYDirAdj() - first.getHeightDir();
//        float urx = last.getXDirAdj() + last.getWidthDirAdj();
//        float ury = first.getYDirAdj();
//
//        // 定义高亮区域
//        PDRectangle rect = new PDRectangle(llx, lly, urx - llx, ury - lly);
//        PDAnnotationHighlight highlight = new PDAnnotationHighlight();
//        highlight.setRectangle(rect);
//
//        // 设置四边形坐标（用于不规则形状）
//        float[] quadPoints = {
//                llx, ury,    // 左上
//                urx, ury,    // 右上
//                llx, lly,    // 左下
//                urx, lly     // 右下
//        };
//        highlight.setQuadPoints(quadPoints);
//        highlight.setColor(new PDColor(new float[]{1, 1, 0}, PDDeviceRGB.INSTANCE)); // 黄色
//
//        return highlight;
//    }
//
//    // 自定义文本提取器（收集文本位置信息）
//    static class TextPositionExtractor extends PDFTextStripper {
//        private final List<TextPosition> textPositions = new ArrayList<>();
//
//        public TextPositionExtractor() throws IOException {
//            super();
//            setSortByPosition(true);
//        }
//
//        @Override
//        protected void writeString(String text, List<TextPosition> textPositions) {
//            this.textPositions.addAll(textPositions);
//        }
//
//        public List<TextPosition> getTextPositions() {
//            return new ArrayList<>(textPositions);
//        }
//    }
//}
