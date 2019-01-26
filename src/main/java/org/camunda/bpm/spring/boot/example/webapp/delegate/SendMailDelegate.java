package org.camunda.bpm.spring.boot.example.webapp.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

/**import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;*/

//@Component
public class SendMailDelegate implements JavaDelegate {
  @Override
  public void execute(DelegateExecution delegateExecution) throws Exception {

  }
/**
  private static final String CONTEXT_KEY_BASE_URL = "baseUrl";

  @Inject MailProperties mailProperties;

  @Inject JavaMailSender javaMailSender;

  //  @Inject SpringTemplateEngine templateEngine;

  private static final Logger LOGGER = LoggerFactory.getLogger(SendMailDelegate.class);

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    LOGGER.info("hello {}", execution.getVariable("amount"));

    Object mail_template = execution.getVariable("mail_template");
    Object mail_subject = execution.getVariable("mail_subject");
    Object mail_to = execution.getVariable("mail_to");
    Object mail_cc = execution.getVariable("mail_cc");

    if (mail_to == null && mail_cc == null) {
      LOGGER.error("mail_to and mail_cc not set - mail _not_ sent");
      throw new RuntimeException("mail_to and mail_cc not set - mail _not_ sent");
    }

    sendMail(
        mail_to != null ? mail_to.toString() : "",
        mail_cc != null ? mail_cc.toString() : "",
        mail_subject != null ? mail_subject.toString() : "",
        mail_template != null ? mail_template.toString() : "Variable 'mail_template' not found",
        execution.getVariables());
  }

  private void sendMail(
      String t, String c, String subject, String content, Map<String, Object> variables)
      throws MessagingException {
    Recipients to = new Recipients();
    to.emailAddresses = t;

    Recipients cc = new Recipients();
    cc.emailAddresses = c;

    RenderedMailTemplate renderedMailTemplate = new RenderedMailTemplate();
    renderedMailTemplate.subject = renderContent(subject, variables);
    renderedMailTemplate.content = renderContent(content, variables);
    renderedMailTemplate.templateType = probeTemplateType(content);

    List<MailAttachmentDTO> attachments = new ArrayList<>();
    sendEmailImpl(to, cc, renderedMailTemplate, attachments);
  }

  private String renderContent(String template, Map<String, Object> variables) {
    for (Entry<String, Object> entry : variables.entrySet()) {
      template =
          template.replace(
              "${" + entry.getKey() + "}",
              entry.getValue() != null ? entry.getValue().toString() : "NULL");
    }
    template.replace("${" + CONTEXT_KEY_BASE_URL + "}", getBaseUrl());
    return template;
  }

  public static class RenderedMailTemplate {
    public String subject;
    public String content;
    public TemplateType templateType = TemplateType.TEXT;

    public boolean isHtml() {
      return templateType == TemplateType.HTML;
    }

    public String toString() {
      return String.format(
          "templateType=%s\nsubject=%s\ncontent=%s", templateType, subject, content);
    }
  }

  public static class Recipients {
    public String emailAddresses;

    @Override
    public String toString() {
      return emailAddresses;
    }
  }

  public TemplateType probeTemplateType(String content) {
    return content.startsWith("<!DOCTYPE") ? TemplateType.HTML : TemplateType.TEXT;
  }

  private String resolveRecipients(Recipients recipients) {
    List<String> resolvedRecipients = new ArrayList<String>();
    if (recipients != null) {
      resolvedRecipients.addAll(
          Strings.csv(recipients.emailAddresses).collect(Collectors.toList()));
    }
    return resolvedRecipients.stream().distinct().collect(Collectors.joining(","));
  }

  //  public RenderedMailTemplate renderMailTemplate(
  //      String templateName, Map<String, Object> variables) {
  //    IContext context =
  //        new IContext() {
  //
  //          @Override
  //          public Locale getLocale() {
  //            return Locale.forLanguageTag("de");
  //          }
  //
  //          @Override
  //          public boolean containsVariable(String name) {
  //            return CONTEXT_KEY_BASE_URL.equals(name) || variables.containsKey(name);
  //          }
  //
  //          @Override
  //          public Set<String> getVariableNames() {
  //            Set<String> names = new HashSet<>();
  //            names.add(CONTEXT_KEY_BASE_URL);
  //            names.addAll(variables.keySet());
  //            return names;
  //          }
  //
  //          @Override
  //          public Object getVariable(String name) {
  //            if (CONTEXT_KEY_BASE_URL.equals(name)) {
  //              return getBaseUrl();
  //            } else if (variables.containsKey(name)) {
  //              return variables.get(name);
  //            }
  //            return null;
  //          }
  //        };
  //
  //    String subject;
  //    String content = templateEngine.process(templateName, context);
  //
  //    TemplateType templateType = probeTemplateType(content);
  //
  //    switch (templateType) {
  //      case HTML:
  //        {
  //          try {
  //            subject =
  //                XPathFactory.newInstance()
  //                    .newXPath()
  //                    .compile("//head/title/text()")
  //                    .evaluate(new InputSource(new StringReader(content)));
  //            break;
  //          } catch (XPathExpressionException e) {
  //            throw new RuntimeException(e);
  //          }
  //        }
  //      default:
  //      case TEXT:
  //        {
  //          String[] result = content.split("\r\n|\r|\n", 2);
  //          subject = result[0];
  //          content = result.length > 1 ? result[1] : "";
  //          break;
  //        }
  //    }
  //
  //    RenderedMailTemplate renderedMailTemplate = new RenderedMailTemplate();
  //    renderedMailTemplate.subject = subject;
  //    renderedMailTemplate.content = content;
  //    renderedMailTemplate.templateType = templateType;
  //    return renderedMailTemplate;
  //  }

  private void sendEmailImpl(
      Recipients to,
      Recipients cc,
      RenderedMailTemplate renderedMailTemplate,
      List<MailAttachmentDTO> attachments)
      throws MessagingException {
    String resolvedTo = resolveRecipients(to);

    if (Strings.isNullOrEmpty(resolvedTo)) {
      return;
    }

    String resolvedCc = resolveRecipients(cc);

    if (isSimulation()) {
      LOGGER.info(
          "mode=simulation  =>  the following email was _not_ sent:\nto {}\ncc {}\n{}",
          to,
          cc,
          renderedMailTemplate);
      return;
    } else {
      LOGGER.info("sending mail to " + to);
    }

    MimeMessage mimeMessage = javaMailSender.createMimeMessage();
    boolean isMultipart = attachments != null && attachments.size() > 0;
    MimeMessageHelper message = new MimeMessageHelper(mimeMessage, isMultipart, "UTF-8");

    message.setTo(InternetAddress.parse(resolvedTo));

    if (Strings.isNotEmpty(resolvedCc)) {
      message.setCc(InternetAddress.parse(resolvedCc));
    }

    try {
      message.setFrom(getSender(), getSenderAlias());
    } catch (UnsupportedEncodingException e) {
      LOGGER.error("", e);
      message.setFrom(getSender());
    }
    message.setSubject(renderedMailTemplate.subject);
    message.setText(renderedMailTemplate.content, renderedMailTemplate.isHtml());

    if (attachments != null) {
      for (MailAttachmentDTO attachment : attachments) {
        if (Strings.isNullOrEmpty(attachment.getMimeType())) {
          message.addAttachment(
              attachment.getFilename(), new ByteArrayResource(attachment.getData()));
        } else {
          message.addAttachment(
              attachment.getFilename(),
              new ByteArrayResource(attachment.getData()),
              attachment.getMimeType());
        }
      }
    }

    javaMailSender.send(mimeMessage);
  }

  private String getSenderAlias() {
    return mailProperties.getProperties().get("sender-alias");
  }

  private String getSender() {
    return mailProperties.getProperties().get("sender");
  }

  private String getBaseUrl() {
    return mailProperties.getProperties().get("base-url");
  }

  private boolean isSimulation() {
    return Boolean.parseBoolean(mailProperties.getProperties().get("simulation"));
  }*/
}
