package org.example;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.parser.ImageRenderInfo;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextRenderInfo;
import com.itextpdf.text.pdf.parser.Vector;

import lombok.Data;

public class PdfMatchKeyword {

    /**
     * 用于供外部类调用获取关键字所在PDF文件坐标
     * @param filepath
     * @param keyWords
     * @return
     */
    public static List<KeyWordPosition> getKeyWordsByPath(String filepath, String keyWords) {
        List<KeyWordPosition> matchItems = null;
        try{
            PdfReader pdfReader = new PdfReader(filepath);
            matchItems = getKeyWords(pdfReader, keyWords);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return matchItems;
    }

    /**
     * 获取关键字所在PDF坐标
     * @param pdfReader
     * @param keyWords
     * @return
     */
    private static List<KeyWordPosition> getKeyWords(PdfReader pdfReader, String keyWords) {
        int page = 0;

        List<KeyWordPosition> matchItems = new ArrayList<>();
        try{
            int pageNum = pdfReader.getNumberOfPages();
            StringBuilder allText = null;

            //遍历页
            for (page = 1; page <= pageNum; page++) {
                //只记录当页的所有内容，需要记录全部页放在循环外面
                List<ItemPosition> allItems = new ArrayList<>();
                //扫描内容
                MyTextExtractionStrategy myTextExtractionStrategy = new MyTextExtractionStrategy(allItems, page);
                PdfTextExtractor.getTextFromPage(pdfReader, page, myTextExtractionStrategy);
                //当页的文字内容，用于关键词匹配
                allText = new StringBuilder();
                //一个字一个字的遍历
                for (int i=0; i<allItems.size(); i++) {
                    ItemPosition item = allItems.get(i);
                    allText.append(item.getText());
                    //关键字存在连续多个块中
                    if(allText.indexOf(keyWords) != -1) {
                        KeyWordPosition keyWordPosition = new KeyWordPosition();
                        //记录关键词每个字的位置，只记录开始结束标记时会有问题
                        List<ItemPosition> listItem = new ArrayList<>();
                        for(int j=i-keyWords.length()+1; j<=i; j++) {
                            listItem.add(allItems.get(j));
                        }
                        keyWordPosition.setListItem(listItem);
                        keyWordPosition.setText(keyWords);

                        matchItems.add(keyWordPosition);
                        allText.setLength(0);
                    }
                }


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return matchItems;
    }


    /**
     * 添加矩形标记
     * @param oldPath
     * @param newPath
     * @param matchItems 关键词
     * @param color 标记颜色
     * @param lineWidth 线条粗细
     * @param padding 边框内边距
     * @throws DocumentException
     * @throws IOException
     */
    public static void andRectangleMark(String oldPath, String newPath, List<KeyWordPosition> matchItems, BaseColor color, int lineWidth, int padding) throws DocumentException, IOException{
        // 待加水印的文件
        PdfReader reader = new PdfReader(oldPath);
        // 加完水印的文件
        PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(newPath));

        PdfContentByte content;

        // 设置字体
        // 循环对每页插入水印
        for (KeyWordPosition keyWordPosition:matchItems)
        {
            //一个关键词的所有字坐标
            List<ItemPosition> oneKeywordItems = keyWordPosition.getListItem();
            for(int i=0; i<oneKeywordItems.size(); i++) {
                ItemPosition item = oneKeywordItems.get(i);
                ItemPosition preItem = i==0?null:oneKeywordItems.get(i-1);
                // 水印的起始
                content = stamper.getOverContent(item.getPage());
                // 开始写入水印
                content.setLineWidth(lineWidth);
                content.setColorStroke(color);

                //底线
                content.moveTo(item.getRectangle().getLeft()-padding, item.getRectangle().getBottom()-padding);
                content.lineTo(item.getRectangle().getRight()+padding, item.getRectangle().getBottom()-padding);
                if(i!=0 && preItem!=null && (preItem.getRectangle().getBottom()-padding)==(item.getRectangle().getBottom()-padding) && (preItem.getRectangle().getRight()+padding)!=(item.getRectangle().getLeft()-padding)) {
                    content.moveTo(preItem.getRectangle().getRight()+padding, preItem.getRectangle().getBottom()-padding);
                    content.lineTo(item.getRectangle().getLeft()-padding, item.getRectangle().getBottom()-padding);
                }
                //上线
                content.moveTo(item.getRectangle().getLeft()-padding, item.getRectangle().getTop()+padding);
                content.lineTo(item.getRectangle().getRight()+padding, item.getRectangle().getTop()+padding);
                if(i!=0 && preItem!=null && (preItem.getRectangle().getTop()+padding)==(item.getRectangle().getTop()+padding) && (preItem.getRectangle().getRight()+padding)!=(item.getRectangle().getLeft()-padding)) {
                    content.moveTo(preItem.getRectangle().getRight()+padding, preItem.getRectangle().getTop()+padding);
                    content.lineTo(item.getRectangle().getLeft()-padding, item.getRectangle().getTop()+padding);
                }

                //左线
                if(i==0) {
                    content.moveTo(item.getRectangle().getLeft()-padding, item.getRectangle().getBottom()-padding);
                    content.lineTo(item.getRectangle().getLeft()-padding, item.getRectangle().getTop()+padding);
                }
                //右线
                if(i==(oneKeywordItems.size()-1)) {
                    content.moveTo(item.getRectangle().getRight()+padding, item.getRectangle().getBottom()-padding);
                    content.lineTo(item.getRectangle().getRight()+padding, item.getRectangle().getTop()+padding);
                }

                content.stroke();
            }
        }
        stamper.close();
    }

    public static void main(String[] args) throws Exception {
        String keyword = "Java";
        String sourcePdf = "/Users/xiongyunfei/Downloads/input.pdf";
        String watermarkPdf = "/Users/xiongyunfei/Downloads/output.pdf";

        Long start = System.currentTimeMillis();
        System.out.println("开始扫描....");
        List<KeyWordPosition> matchItems = getKeyWordsByPath(sourcePdf, keyword);
        System.out.println("扫描结束["+(System.currentTimeMillis()-start)+"ms]，共找到关键字["+keyword+"]出现["+matchItems.size()+"]次");

        start = System.currentTimeMillis();
        System.out.println("开始添加标记....");
        andRectangleMark(sourcePdf
                , watermarkPdf
                , matchItems
                , BaseColor.RED
                , 2
                , 2);
        System.out.println("标记添加完成["+(System.currentTimeMillis()-start)+"ms]");
    }
}

/**
 * @ClassName: ItemPosition
 * @Description: 字体的位置信息
 * @author chenyang-054
 * @date 2021-04-09 09:14:36
 */
@Data
class ItemPosition{
    private Integer page;
    private String text;
    //这个字的矩形坐标
    private Rectangle rectangle;
}

/**
 * @ClassName: KeyWordPosition
 * @Description: 需要高亮显示的关键字坐标信息
 * @author chenyang-054
 * @date 2021-04-09 11:28:56
 */
@Data
class KeyWordPosition{
    private String text;
    private List<ItemPosition> listItem;
}

/**
 * @ClassName: MyTextExtractionStrategy
 * @Description: 记录所有位置+字体信息，这种方式获取坐标信息和字体信息方便一点
 * @author chenyang-054
 * @date 2021-04-09 11:00:31
 */
class MyTextExtractionStrategy implements TextExtractionStrategy{

    private List<ItemPosition> positions;
    private Integer page;

    public MyTextExtractionStrategy() {}

    public MyTextExtractionStrategy(List<ItemPosition> positions, Integer page) {
        this.positions = positions;
        this.page = page;
    }

    @Override
    public void beginTextBlock() {
        // TODO Auto-generated method stub

    }

    @Override
    public void renderText(TextRenderInfo renderInfo) {
        ItemPosition ItemPosition = new ItemPosition();
        Vector bottomLeftPoint = renderInfo.getDescentLine().getStartPoint();
        Vector topRightPoint = renderInfo.getAscentLine().getEndPoint();
        //记录矩形坐标
        Rectangle rectangle = new Rectangle(bottomLeftPoint.get(Vector.I1), bottomLeftPoint.get(Vector.I2),
                topRightPoint.get(Vector.I1), topRightPoint.get(Vector.I2));
        ItemPosition.setPage(page);
        ItemPosition.setRectangle(rectangle);
        ItemPosition.setText(renderInfo.getText());
        positions.add(ItemPosition);
    }

    @Override
    public void endTextBlock() {
        // TODO Auto-generated method stub

    }

    @Override
    public void renderImage(ImageRenderInfo renderInfo) {
        // TODO Auto-generated method stub

    }

    @Override
    public String getResultantText() {
        // TODO Auto-generated method stub
        return null;
    }
}
