module zloader {
    requires static lombok;
    requires com.fasterxml.jackson.databind;
    requires org.slf4j;
    requires jcommander;
    requires org.apache.logging.log4j;
    opens net.woggioni.jpms.loader.zloader.api;
}
