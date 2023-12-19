package jasony62.camel;

import java.io.InputStream;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.attachment.Attachment;
import org.apache.camel.attachment.AttachmentMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.activation.DataHandler;

/**
 * 处理单个文件
 * 将文件放到message的body中
 */
public class FileProcessor implements Processor {

  private static final Logger LOG = LoggerFactory.getLogger(FileProcessor.class);

  private String filePartName = "file";
  /**
   * 当前处理的文件名
   */
  private String processedFilenamePropKey = "processedFilename";

  public void setFilePartName(String value) {
    filePartName = value;
  }

  public String getFilePartName() {
    return filePartName;
  }

  public String getProcessedFilenamePropKey() {
    return processedFilenamePropKey;
  }

  public void setProcessedFilenamePropKey(String processedFilenamePropKey) {
    this.processedFilenamePropKey = processedFilenamePropKey;
  }

  public void process(Exchange exchange) throws Exception {
    LOG.debug("进入FileProcessor");
    AttachmentMessage am = exchange.getMessage(AttachmentMessage.class);
    Map<String, Attachment> aos = am.getAttachmentObjects();

    Attachment a = aos.get(filePartName);
    String hvalue = a.getHeader("content-disposition");
    String filename = "";
    for (String s : hvalue.split(";")) {
      if (s.contains("filename=")) {
        filename = s.replace("filename=", "").replace("\"", "").trim();
        break;
      }
    }
    exchange.setProperty(processedFilenamePropKey, filename);

    DataHandler dh = a.getDataHandler();
    InputStream is = dh.getInputStream();
    exchange.getIn().setBody(is);
  }

}