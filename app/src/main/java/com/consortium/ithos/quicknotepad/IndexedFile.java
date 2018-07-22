package com.consortium.ithos.quicknotepad;

import java.io.File;

/**
 * Created by ithos on 7/20/18.
 */
public class IndexedFile {
    public File storedFile;
    public int index;

    public IndexedFile(File file, int idx)
    {
        storedFile = file;
        index = idx;
    }
}
