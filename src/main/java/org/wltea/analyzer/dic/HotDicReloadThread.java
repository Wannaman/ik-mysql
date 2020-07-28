package org.wltea.analyzer.dic;

import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.logging.ESLoggerFactory;

/**
 * @author yuqiqi
 * @date 2020/7/7 11:14
 */
public class HotDicReloadThread implements Runnable{
    private static final Logger logger = ESLoggerFactory.getLogger(HotDicReloadThread.class.getName());

    @Override
    public void run() {
        while (true){
            logger.info("-------重新加载mysql词典--------");

            Dictionary.getSingleton().reLoadMainDict();
        }
    }
}
