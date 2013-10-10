package edu.neumont.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExtensionFilter implements FilenameFilter, FileFilter {
	public static final ExtensionFilter JARS = new ExtensionFilter( "jar" );
	public static final ExtensionFilter IMAGES = new ExtensionFilter("png","jpg","jpeg","bmp","gif");
	
	private List<String> extensions;
	
	public ExtensionFilter( String ... extensions) {
		if( extensions == null ) {
			this.extensions = new ArrayList<String>();
		} else {
			this.extensions = Arrays.asList( extensions );
		}
	}
	
	@Override
	public boolean accept(File dir, String name) {
		String ext = name.substring(name.lastIndexOf('.')+1);
		return extensions.contains( ext );
	}
	
	public String[] getSupportedExtensions() {
		return extensions.toArray(new String[0]);
	}

	@Override
	public boolean accept(File pathname) {
		return accept( new File(pathname.getParent()), pathname.getName());
	}
}
