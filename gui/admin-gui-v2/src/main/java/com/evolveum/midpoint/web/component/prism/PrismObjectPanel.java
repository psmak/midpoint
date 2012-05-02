/*
 * Copyright (c) 2012 Evolveum
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.opensource.org/licenses/cddl1 or
 * CDDLv1.0.txt file in the source code distribution.
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 *
 * Portions Copyrighted 2012 [name of copyright owner]
 */

package com.evolveum.midpoint.web.component.prism;

import com.evolveum.midpoint.web.component.util.VisibleEnableBehaviour;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import java.util.List;

/**
 * @author lazyman
 */
public class PrismObjectPanel extends Panel {

    private boolean showHeader = true;

    public PrismObjectPanel(String id, IModel<ObjectWrapper> model, ResourceReference image) {
        super(id);
        setOutputMarkupId(true);

        initLayout(model, image);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);

        response.renderCSSReference(new PackageResourceReference(PrismObjectPanel.class, "PrismObjectPanel.css"));
    }

    private void initLayout(final IModel<ObjectWrapper> model, ResourceReference image) {
        WebMarkupContainer headerPanel = new WebMarkupContainer("headerPanel");
        add(headerPanel);
        headerPanel.add(new VisibleEnableBehaviour() {

            @Override
            public boolean isVisible() {
                return isShowHeader();
            }
        });
        headerPanel.add(new Label("header", createDisplayName(model)));
        headerPanel.add(new Label("description", createDescription(model)));

        Image headerImg = new Image("headerImg", image);
        headerPanel.add(headerImg);

        initButtons(headerPanel, model);

        WebMarkupContainer body = new WebMarkupContainer("body");
        body.add(new VisibleEnableBehaviour() {

            @Override
            public boolean isVisible() {
                ObjectWrapper wrapper = model.getObject();
                return !wrapper.isMinimalized();
            }
        });
        add(body);

        ListView<ContainerWrapper> containers = new ListView<ContainerWrapper>("containers",
                new PropertyModel<List<ContainerWrapper>>(model, "containers")) {

            @Override
            protected void populateItem(ListItem<ContainerWrapper> item) {
                item.add(new PrismContainerPanel("container", item.getModel()));
            }
        };
        body.add(containers);

        WebMarkupContainer footer = createFooterPanel("footer", model);
        if (!(footer instanceof EmptyPanel)) {
            footer.add(new VisibleEnableBehaviour() {

                @Override
                public boolean isVisible() {
                    ObjectWrapper wrapper = model.getObject();
                    return wrapper.isMinimalized();
                }
            });
        } else {
            footer.setVisible(false);
        }
        add(footer);
    }

    public WebMarkupContainer createFooterPanel(String footerId, IModel<ObjectWrapper> model) {
        return new EmptyPanel(footerId);
    }

    protected IModel<String> createDisplayName(IModel<ObjectWrapper> model) {
        return new PropertyModel<String>(model, "displayName");
    }

    protected IModel<String> createDescription(IModel<ObjectWrapper> model) {
        return new PropertyModel<String>(model, "description");
    }

    private void initButtons(WebMarkupContainer headerPanel, final IModel<ObjectWrapper> model) {
        headerPanel.add(new PrismOptionButtonPanel("optionButtons", model) {

            @Override
            public void checkBoxOnUpdate(AjaxRequestTarget target) {
                ObjectWrapper wrapper = model.getObject();
                wrapper.setSelected(!wrapper.isSelected());
                target.add(PrismObjectPanel.this);
                super.checkBoxOnUpdate(target);
            }

            @Override
            public void minimizeOnClick(AjaxRequestTarget target) {
                ObjectWrapper wrapper = model.getObject();
                wrapper.setMinimalized(!wrapper.isMinimalized());
                target.add(PrismObjectPanel.this);
                super.minimizeOnClick(target);
            }

            @Override
            public void showEmptyOnClick(AjaxRequestTarget target) {
                ObjectWrapper wrapper = model.getObject();
                wrapper.setShowEmpty(!wrapper.isShowEmpty());
                target.add(PrismObjectPanel.this);
                super.showEmptyOnClick(target);
            }
        });
        headerPanel.add(createOperationPanel("operationButtons"));
    }

    protected Panel createOperationPanel(String id) {
        return new EmptyPanel(id);
    }

    public boolean isShowHeader() {
        return showHeader;
    }

    public void setShowHeader(boolean showHeader) {
        this.showHeader = showHeader;
    }
}
