package com.axonivy.utils.persistence.test.ui;

import java.io.FileNotFoundException;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import com.axonivy.utils.persistence.test.service.TestService;


@ManagedBean(name = "test")
@ViewScoped
public class TestBean {

	public StreamedContent getDBExport() throws FileNotFoundException {

		StreamedContent content = DefaultStreamedContent.builder()
				.stream(() -> TestService.getDBExport())
				.name("testdata.xls")
				.contentType("application/vnd.ms-excel")
				.build();

		return content;
	}
}
