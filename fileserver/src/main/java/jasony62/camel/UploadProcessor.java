package jasony62.camel;

import java.util.Map;
import java.util.ArrayList;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.attachment.Attachment;
import org.apache.camel.attachment.AttachmentMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 预处理请求，提取关键信息
 */
public class UploadProcessor implements Processor {

  private static final Logger LOG = LoggerFactory.getLogger(UploadProcessor.class);
  /**
   * 文件分段名称列表在exchange中属性的名称
   */
  private String filePartNamesPropKey = "filePartNames";
  /**
   * 非文件分段名称列表在exchange中属性的名称
   */
  private String fieldPartNamesPropKey = "fieldPartNames";

  /**
   * 表单中的每一个分段内容对应一个attachment
   */
  @Override
  public void process(Exchange exchange) throws Exception {
    LOG.debug("进入ProfileProcessor");
    /**
     * 处理请求中携带的attachment
     */
    List<String> filePartNames = new ArrayList<String>();
    List<String> fieldPartNames = new ArrayList<String>();
    AttachmentMessage am = exchange.getMessage(AttachmentMessage.class);
    Map<String, Attachment> aos = am.getAttachmentObjects();
    for (Map.Entry<String, Attachment> entry : aos.entrySet()) {
      String partName = entry.getKey();
      Attachment a = entry.getValue();
      for (String hname : a.getHeaderNames()) {
        String hvalue = a.getHeader(hname);
        /**
         * 如果头中包含filename，认为是文件
         */
        if (hname.equals("content-disposition")) {
          if (hvalue.contains("filename=")) {
            filePartNames.add(partName);
          } else {
            fieldPartNames.add(partName);
          }
          break;
        }
      }
    }
    /**
     * 保存提取的信息
     */
    exchange.setProperty(filePartNamesPropKey, filePartNames);
    exchange.setProperty(fieldPartNamesPropKey, fieldPartNames);
  }

}