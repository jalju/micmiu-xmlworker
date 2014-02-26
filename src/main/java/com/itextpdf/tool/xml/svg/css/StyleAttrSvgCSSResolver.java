/*
 * $Id: StyleAttrSvgCSSResolver.java 341 2012-06-18 14:38:54Z blowagie $
 *
 * This file is part of the iText (R) project.
 * Copyright (c) 1998-2012 1T3XT BVBA
 * Authors: VVB, Bruno Lowagie, et al.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License version 3
 * as published by the Free Software Foundation with the addition of the
 * following permission added to Section 15 as permitted in Section 7(a):
 * FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY 1T3XT,
 * 1T3XT DISCLAIMS THE WARRANTY OF NON INFRINGEMENT OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA, 02110-1301 USA, or download the license from the following URL:
 * http://itextpdf.com/terms-of-use/
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License,
 * a covered work must retain the producer line in every PDF that is created
 * or manipulated using iText.
 *
 * You can be released from the requirements of the license by purchasing
 * a commercial license. Buying such a license is mandatory as soon as you
 * develop commercial activities involving the iText software without
 * disclosing the source code of your own applications.
 * These activities include: offering paid services to customers as an ASP,
 * serving PDFs on the fly in a web application, shipping iText with a closed
 * source product.
 *
 * For more information, please contact iText Software Corp. at this
 * address: sales@itextpdf.com
 */
package com.itextpdf.tool.xml.svg.css;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;


import com.itextpdf.tool.xml.Tag;
import com.itextpdf.tool.xml.css.CSS;
import com.itextpdf.tool.xml.css.CssFile;
import com.itextpdf.tool.xml.css.CssFileProcessor;
import com.itextpdf.tool.xml.css.CssFiles;
import com.itextpdf.tool.xml.css.CssFilesImpl;
import com.itextpdf.tool.xml.css.CssInheritanceRules;
import com.itextpdf.tool.xml.css.CssUtils;
import com.itextpdf.tool.xml.css.DefaultCssInheritanceRules;
import com.itextpdf.tool.xml.css.StyleAttrCSSResolver;
import com.itextpdf.tool.xml.exceptions.CssResolverException;
import com.itextpdf.tool.xml.html.HTML;
import com.itextpdf.tool.xml.net.FileRetrieve;
import com.itextpdf.tool.xml.net.FileRetrieveImpl;
import com.itextpdf.tool.xml.pipeline.css.CSSResolver;
import com.itextpdf.tool.xml.svg.graphic.SVGAttributes;


public class StyleAttrSvgCSSResolver implements CSSResolver {

	/**
	 *
	 */
	public static final String STYLE = HTML.Attribute.STYLE;
	private final CssUtils utils;
	private CssInheritanceRules inherit;
	private final CssFiles cssFiles;
	private FileRetrieve retrieve;
	private Map<String, Integer> attributes;

	/**
	 * Construct a new {@link StyleAttrCSSResolver} with default settings.
	 */
	public StyleAttrSvgCSSResolver() {
		this(new CssFilesImpl(), CssUtils.getInstance());
	}

	/**
	 * Construct a new StyleAttrCSSResolver with the given {@link CssFiles} and the {@link DefaultCssInheritanceRules}.
	 *
	 * @param cssFiles a {@link CssFiles} implementation.
	 */
	public StyleAttrSvgCSSResolver(final CssFiles cssFiles) {
		this(cssFiles, CssUtils.getInstance());

	}

	/**
	 * Construct a new StyleAttrCSSResolver with the given {@link CssFiles} and {@link CssUtils} and the
	 * {@link DefaultCssInheritanceRules}.
	 *
	 * @param cssFiles a {@link CssFiles} implementation.
	 * @param utils the CssUtils to use.
	 */
	public StyleAttrSvgCSSResolver(final CssFiles cssFiles, final CssUtils utils) {
		this(new DefaultCssInheritanceRules(), cssFiles, utils);
	}

	/**
	 * Construct a new StyleAttrCSSResolver with the given {@link CssFiles} and {@link CssUtils}.
	 *
	 * @param rules the {@link CssInheritanceRules} to use.
	 * @param cssFiles a {@link CssFiles} implementation.
	 * @param utils the CssUtils to use.
	 */
	public StyleAttrSvgCSSResolver(final CssInheritanceRules rules, final CssFiles cssFiles, final CssUtils utils) {
		this(rules, cssFiles, utils, new FileRetrieveImpl());
	}
	/**
	 * Construct a new StyleAttrCSSResolver with the given {@link CssFiles} and {@link CssUtils}.
	 *
	 * @param rules the {@link CssInheritanceRules} to use.
	 * @param cssFiles a {@link CssFiles} implementation.
	 * @param utils the CssUtils to use.
	 * @param fileRetrieve the {@link FileRetrieve} implementation
	 */
	public StyleAttrSvgCSSResolver(final CssInheritanceRules rules, final CssFiles cssFiles, final CssUtils utils, final FileRetrieve fileRetrieve) {
		this.utils = utils;
		this.cssFiles = cssFiles;
		this.inherit = rules;
		this.retrieve = fileRetrieve;
		this.attributes = SVGAttributes.getSVGAttributesList();
	}

	/**
	 * @param cssFiles the {@link CssFile} implementation
	 * @param r the {@link FileRetrieve} implementation
	 */
	public StyleAttrSvgCSSResolver(final CssFiles cssFiles, final FileRetrieve r) {
		this(new DefaultCssInheritanceRules(), cssFiles, CssUtils.getInstance(), r);
	}

	/**
	 * Also taking into account the CSS properties of any parent tag in the given tag.
	 *
	 * @see com.itextpdf.tool.xml.pipeline.css.CSSResolver#resolveStyles(com.itextpdf.tool.xml.Tag)
	 */
	
	//THE DIFFERENCE BETWEEN HTML AND SVG: SVG has for a lot of style properties the possibility to use still attributes that define the same 
	public void resolveStyles(final Tag t) {
		// get css for this tag from resolver
		Map<String, String> tagCss = new HashMap<String, String>();
		if (null != cssFiles && cssFiles.hasFiles()) {
			tagCss = cssFiles.getCSS(t);
		}
		
		if (null != t.getAttributes() && !t.getAttributes().isEmpty()) {
			//first get the attributes that related to style but aren't in a style attribute, these can be overwritten by the same element that is defined in a style
			//TODO check default values & incorrect values: 
			//e.g. stroke="red" style="stroke:yellow" -> yellow but stroke="red" style="stroke:an non-existing color" -> red
			//if both are wrong or missing -> take from parent 
			
			for (String key : t.getAttributes().keySet()) {
				String value =  t.getAttributes().get(key);
				boolean valid = SVGAttributes.isValidAttribute(key, value, attributes);
				if(valid){
					tagCss.put(key, value);
				}
			}

			// get css from "style" attr			
			String styleAtt = t.getAttributes().get(HTML.Attribute.STYLE);
			if (null != styleAtt && styleAtt.length() > 0) {
				String[] styles = styleAtt.split(";");
				for (String s : styles) {
					String[] part = s.split(":",2);
					if (part.length == 2) {
						String key = part[0].trim();
						String value = utils.stripDoubleSpacesAndTrim(part[1]);
						
						//ONLY add when it is a valid style attribute in SVG
						if(SVGAttributes.isValidAttribute(key, value, attributes)){
							tagCss.put(key, value);
						}else{
							//System.out.println(key + " " + value);
						}
						
						//splitRules(tagCss, key, value);
					}
				}
			}
		}
		// inherit css from parent tags, as defined in provided CssInheritanceRules or if property = inherit
		Map<String, String> css = t.getCSS();
		if (mustInherit(t.getName()) && null != t.getParent() && null != t.getParent().getCSS()) {
			if (null != this.inherit) {
				for (Entry<String, String> entry : t.getParent().getCSS().entrySet()) {
					String key = entry.getKey();
					if ((tagCss.containsKey(key) && CSS.Value.INHERIT.equals(tagCss.get(key)) ) || canInherite(t, key)) {
						//splitRules(css, key, entry.getValue());
						
						if(SVGAttributes.isValidAttribute(key, entry.getValue(), attributes)){
							css.put(key, entry.getValue());
						}	
					}
				}
			} else {
				css.putAll(t.getParent().getCSS());
			}
		}
		// overwrite properties (if value != inherit)
		for (Entry<String, String> e : tagCss.entrySet()) {
			if (!CSS.Value.INHERIT.equalsIgnoreCase(e.getValue())) {
				css.put(e.getKey(), e.getValue());
			}
		}

	}
		
	
	/**
	 * @param css the css map to populate
	 * @param key the property
	 * @param value the value
	private void splitRules(final Map<String, String> css, final String key, final String value) {
		if (CSS.Property.BORDER.equalsIgnoreCase(key)) {
			css.putAll(utils.parseBorder(value));
		} else if (CSS.Property.MARGIN.equalsIgnoreCase(key)) {
			css.putAll(utils.parseBoxValues(value, "margin-", ""));
		} else if (CSS.Property.BORDER_WIDTH.equalsIgnoreCase(key)) {
			css.putAll(utils.parseBoxValues(value, "border-", "-width"));
		} else if (CSS.Property.BORDER_STYLE.equalsIgnoreCase(key)) {
			css.putAll(utils.parseBoxValues(value, "border-", "-style"));
		} else if (CSS.Property.BORDER_COLOR.equalsIgnoreCase(key)) {
			css.putAll(utils.parseBoxValues(value, "border-", "-color"));
		} else if (CSS.Property.PADDING.equalsIgnoreCase(key)) {
			css.putAll(utils.parseBoxValues(value, "padding-", ""));
		} else if (CSS.Property.FONT.equalsIgnoreCase(key)) {
			css.putAll(utils.processFont(value));
		} else if (CSS.Property.LIST_STYLE.equalsIgnoreCase(key)) {
			css.putAll(utils.processListStyle(value));
		} else {
			css.put(key, value);
		}
	}
	 */

	/**
	 * By setting an implementation of {@link CssInheritanceRules} a developer can set rules on what css selectors are
	 * inherited from parent tags.
	 *
	 * @param cssInheritanceRules the inherit to set
	 */
	public void setCssInheritance(final CssInheritanceRules cssInheritanceRules) {
		this.inherit = cssInheritanceRules;
	}

	/**
	 * Defaults to true if no {@link CssInheritanceRules} implementation set.
	 *
	 * @param t
	 * @param property
	 * @return true if may be inherited false otherwise
	 */
	private boolean canInherite(final Tag t, final String property) {
		if (null != this.inherit) {
			return this.inherit.inheritCssSelector(t, property);
		}
		return true;
	}

	/**
	 * Defaults to true if no {@link CssInheritanceRules} implementation set.
	 *
	 * @param tag
	 * @return true if must be inherited false otherwise
	 */
	private boolean mustInherit(final String tag) {
		if (null != this.inherit) {
			return this.inherit.inheritCssTag(tag);
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see com.itextpdf.tool.xml.pipeline.css.CSSResolver#addCss(java.lang.String, java.lang.String)
	 */
	public void addCss(final String content, final String charSet, final boolean isPersistent) throws CssResolverException {
		CssFileProcessor proc = new CssFileProcessor();
		try {
			retrieve.processFromStream(new ByteArrayInputStream(content.getBytes(charSet)), proc);
			CssFile css = proc.getCss();
			css.isPersistent(isPersistent);
			this.cssFiles.add(css);
		} catch (UnsupportedEncodingException e) {
			throw new CssResolverException(e);
		} catch (IOException e) {
			throw new CssResolverException(e);
		}
	}

	/**
	 * Add a file to the CssFiles Collection.
	 *
	 * @param href the path, if it starts with http we try to retrieve the file
	 *            from the net, if not we try a normal file operation.
	 */
	public void addCssFile(final String href, final boolean isPersistent) throws CssResolverException {
		CssFileProcessor cssFileProcessor = new CssFileProcessor();
		try {
			retrieve.processFromHref(href, cssFileProcessor);
		} catch (IOException e) {
			throw new CssResolverException(e);
		}
		CssFile css = cssFileProcessor.getCss();
		css.isPersistent(isPersistent);
		this.cssFiles.add(css);
	}

	/**
	 * Add a file to the CssFiles Collection.
	 * @param file the CssFile to add.
	 */
	public void addCss(final CssFile file) {
		this.cssFiles.add(file);
	}

	/* (non-Javadoc)
	 * @see com.itextpdf.tool.xml.pipeline.css.CSSResolver#addCss(java.lang.String)
	 */
	public void addCss(final String content, final boolean isPersistent) throws CssResolverException {
		CssFileProcessor proc = new CssFileProcessor();
		FileRetrieve retrieve = new FileRetrieveImpl();
		try {
			retrieve.processFromStream(new ByteArrayInputStream(content.getBytes()), proc);
			CssFile css = proc.getCss();
			css.isPersistent(isPersistent);
			this.cssFiles.add(css);
		} catch (UnsupportedEncodingException e) {
			throw new CssResolverException(e);
		} catch (IOException e) {
			throw new CssResolverException(e);
		}

	}

	/**
	 * @param inherit the inherit to set
	 */
	public void setCssInheritanceRules(final CssInheritanceRules inherit) {
		this.inherit = inherit;
	}

	/**
	 * The {@link FileRetrieve} implementation to use in {@link StyleAttrCSSResolver#addCss(String, boolean)}.
	 * @param retrieve the retrieve to set
	 */
	public void setFileRetrieve(final FileRetrieve retrieve) {
		this.retrieve = retrieve;
	}

	/* (non-Javadoc)
	 * @see com.itextpdf.tool.xml.pipeline.css.CSSResolver#clear()
	 */
	public CSSResolver clear() throws CssResolverException {
		cssFiles.clear();
		return this;
	}


}
