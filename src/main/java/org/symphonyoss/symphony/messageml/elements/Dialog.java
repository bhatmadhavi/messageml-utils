package org.symphonyoss.symphony.messageml.elements;

import static org.apache.commons.lang3.StringUtils.containsWhitespace;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import org.symphonyoss.symphony.messageml.MessageMLContext;
import org.symphonyoss.symphony.messageml.MessageMLParser;
import org.symphonyoss.symphony.messageml.bi.BiContext;
import org.symphonyoss.symphony.messageml.bi.BiFields;
import org.symphonyoss.symphony.messageml.exceptions.InvalidInputException;
import org.symphonyoss.symphony.messageml.markdown.nodes.form.DialogNode;
import org.symphonyoss.symphony.messageml.util.ShortID;
import org.symphonyoss.symphony.messageml.util.XmlPrintStream;
import org.w3c.dom.Node;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents the Symphony Element Dialog which is represented with tag name "dialog".
 * The messageML representation of the dialog element can contain the following attributes:
 * <ul>
 *   <li> id (required) -> used to identify the dialog </li>
 *   <li> width -> used to specify dialog width, must be among values "small", "medium", "large", "full-width". Default value is medium</li>
 *   <li> state -> used to specify dialog state, must be among values "open", "close". Default value is "close"</li>
 * </ul>
 * It can contain the following child tags:
 * <ul>
 *   <li> title (required) -> used to specify dialog title, of type {@link DialogChild.Title}</li>
 *   <li> body (required) -> used to specify dialog body, of type {@link DialogChild.Body}</li>
 *   <li> footer -> used to specify dialog footer, of type {@link DialogChild.Footer}</li>
 * </ul>
 */
public class Dialog extends Element {

  public static final String MESSAGEML_TAG = "dialog";
  public static final int ID_MAX_LENGTH = 64;

  public static final String STATE_ATTR = "state";
  public static final String CLOSE_STATE = "close";
  public static final List<String> ALLOWED_STATE_VALUES = Arrays.asList("open", CLOSE_STATE);

  public static final String WIDTH_ATTR = "width";
  public static final String MEDIUM_WIDTH = "medium";
  public static final List<String> ALLOWED_WIDTH_VALUES = Arrays.asList("small", MEDIUM_WIDTH, "large", "full-width");

  private static final String DATA_ATTRIBUTE_PREFIX = "data-";
  private static final String OPEN_ATTRIBUTE = "open";

  private static final ShortID SHORT_ID = new ShortID();

  public Dialog(Element parent, FormatEnum format) {
    super(parent, MESSAGEML_TAG, format);
  }

  @Override
  public Boolean hasIdAttribute() {
    return true;
  }

  @Override
  protected void buildAttribute(MessageMLParser parser, Node item) throws InvalidInputException {
    switch (item.getNodeName()) {
      case ID_ATTR:
      case WIDTH_ATTR:
      case STATE_ATTR:
        setAttribute(item.getNodeName(), item.getNodeValue());
        break;
      default:
        throwInvalidInputException(item);
    }
  }

  @Override
  public void validate() throws InvalidInputException {
    super.validate();

    checkAttributes();
    validateChildrenTypes();
  }

  @Override
  public void asPresentationML(XmlPrintStream out, MessageMLContext context) {
    out.openElement(getPresentationMLTag(), getPresentationMLAttributes());
    for (Element child : getChildren()) {
      child.asPresentationML(out, context);
    }
    out.closeElement();
  }

  @Override
  org.commonmark.node.Node asMarkdown() throws InvalidInputException {
    return new DialogNode();
  }

  @Override
  void updateBiContext(BiContext context) {
    super.updateBiContext(context);
    context.updateItemCount(BiFields.POPUPS.getValue());
  }

  private void checkAttributes() throws InvalidInputException {
    validateIdAttribute();
    checkAttributeOrPutDefaultValue(WIDTH_ATTR, MEDIUM_WIDTH, ALLOWED_WIDTH_VALUES);
    checkAttributeOrPutDefaultValue(STATE_ATTR, CLOSE_STATE, ALLOWED_STATE_VALUES);
  }

  private void validateIdAttribute() throws InvalidInputException {
    String id = getAttribute(ID_ATTR);
    if (isEmpty(id) || containsWhitespace(id)) {
      throw new InvalidInputException("The attribute \"id\" is required and must not contain any whitespace");
    }
    assertAttributeMaxLength(ID_ATTR, ID_MAX_LENGTH);
  }

  private void checkAttributeOrPutDefaultValue(String attributeName, String defaultValue, List<String> allowedValues)
      throws InvalidInputException {
    if (getAttribute(attributeName) == null) {
      setAttribute(attributeName, defaultValue);
    }
    assertAttributeValue(attributeName, allowedValues);
  }

  private void validateChildrenTypes() throws InvalidInputException {
    assertContainsAlwaysChildOfType(Collections.singleton(DialogChild.Title.class));
    assertContainsAlwaysChildOfType(Collections.singleton(DialogChild.Body.class));
    validateNoOtherChildrenTypes();
  }

  private void validateNoOtherChildrenTypes() throws InvalidInputException {
    final boolean areAllChildrenOfAllowedTypes =
        getChildren().stream().allMatch(element -> element instanceof DialogChild);
    if (!areAllChildrenOfAllowedTypes) {
      throw new InvalidInputException("A dialog can only contain tags \"title\", \"body\", \"footer\"");
    }
  }

  private Map<String, String> getPresentationMLAttributes() {
    Map<String, String> pmlAttributes = new HashMap<>();

    pmlAttributes.put(OPEN_ATTRIBUTE, null);
    for (Map.Entry<String, String> mmlAttribute : getAttributes().entrySet()) {
      if (mmlAttribute.getKey().equals(ID_ATTR)) {
        pmlAttributes.put(mmlAttribute.getKey(), SHORT_ID.generate() + "-" + mmlAttribute.getValue());
      } else {
        pmlAttributes.put(DATA_ATTRIBUTE_PREFIX + mmlAttribute.getKey(), mmlAttribute.getValue());
      }
    }
    return pmlAttributes;
  }
}