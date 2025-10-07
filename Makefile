SRC_DIR = src
BIN_DIR = bin
PACKAGE = com/scheduler
MAIN_CLASS = com.scheduler.Escalonador

SOURCES = $(shell find $(SRC_DIR) -name "*.java")

CLASSES = $(SOURCES:$(SRC_DIR)/%.java=$(BIN_DIR)/%.class)

.PHONY: all clean run

all: $(CLASSES)

$(BIN_DIR)/%.class: $(SRC_DIR)/%.java
	@mkdir -p $(dir $@)
	@javac -d $(BIN_DIR) -sourcepath $(SRC_DIR) $<

clean:
	rm -rf $(BIN_DIR)

run: all
	@java -cp $(BIN_DIR) $(MAIN_CLASS)
