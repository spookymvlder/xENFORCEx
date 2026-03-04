package com.spookymvlder.formtools.datatypes;

final class PathFrame {
    final String name;
    final Integer index; // null = no index

    PathFrame(String name, Integer index) {
        this.name = name;
        this.index = index;
    }
}
