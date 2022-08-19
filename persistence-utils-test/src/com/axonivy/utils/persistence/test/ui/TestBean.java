package com.axonivy.utils.persistence.test.ui;

import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import com.axonivy.utils.persistence.test.service.TestService;


@ManagedBean(name = "test")
@ViewScoped
public class TestBean {

	public StreamedContent getDBExport() throws FileNotFoundException {

		InputStream dbExportStream = TestService.getDBExport();

		StreamedContent content = new DefaultStreamedContent(dbExportStream, "application/vnd.ms-excel", "testdata.xls");

		return content;
	}
}
