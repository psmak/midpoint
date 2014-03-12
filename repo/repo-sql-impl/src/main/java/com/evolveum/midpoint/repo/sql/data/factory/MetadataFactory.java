package com.evolveum.midpoint.repo.sql.data.factory;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.repo.sql.data.common.Metadata;
import com.evolveum.midpoint.repo.sql.data.common.other.RReferenceOwner;
import com.evolveum.midpoint.repo.sql.util.DtoTranslationException;
import com.evolveum.midpoint.repo.sql.util.RUtil;
import com.evolveum.midpoint.xml.ns._public.common.common_2a.MetadataType;
import org.apache.commons.lang.Validate;

import java.util.List;

/**
 * @author lazyman
 */
public class MetadataFactory {

    public static void copyToJAXB(Metadata repo, MetadataType jaxb, PrismContext prismContext)
            throws DtoTranslationException {
        Validate.notNull(repo, "Repo metadata must not be null.");
        Validate.notNull(jaxb, "Jaxb metadata must not be null.");
        Validate.notNull(prismContext, "Prism context must not be null.");

        jaxb.setCreateChannel(repo.getCreateChannel());
        jaxb.setCreateTimestamp(repo.getCreateTimestamp());
        jaxb.setModifyChannel(repo.getModifyChannel());
        jaxb.setModifyTimestamp(repo.getModifyTimestamp());

        if (repo.getCreatorRef() != null) {
            jaxb.setCreatorRef(repo.getCreatorRef().toJAXB(prismContext));
        }
        if (repo.getModifierRef() != null) {
            jaxb.setModifierRef(repo.getModifierRef().toJAXB(prismContext));
        }

        List refs = RUtil.safeSetReferencesToList(repo.getCreateApproverRef(), prismContext);
        if (!refs.isEmpty()) {
            jaxb.getCreateApproverRef().addAll(refs);
        }
        refs = RUtil.safeSetReferencesToList(repo.getModifyApproverRef(), prismContext);
        if (!refs.isEmpty()) {
            jaxb.getModifyApproverRef().addAll(refs);
        }
    }

    public static void copyFromJAXB(MetadataType jaxb, Metadata repo, PrismContext prismContext)
            throws DtoTranslationException {
        Validate.notNull(repo, "Repo metadata must not be null.");
        Validate.notNull(jaxb, "Jaxb metadata must not be null.");
        Validate.notNull(prismContext, "Prism context must not be null.");

        repo.setCreateChannel(jaxb.getCreateChannel());
        repo.setCreateTimestamp(jaxb.getCreateTimestamp());
        repo.setModifyChannel(jaxb.getModifyChannel());
        repo.setModifyTimestamp(jaxb.getModifyTimestamp());

        repo.setCreatorRef(RUtil.jaxbRefToEmbeddedRepoRef(jaxb.getCreatorRef(), prismContext));
        repo.setModifierRef(RUtil.jaxbRefToEmbeddedRepoRef(jaxb.getModifierRef(), prismContext));

        //todo fix
//        repo.getCreateApproverRef().addAll(RUtil.safeListReferenceToSet(jaxb.getCreateApproverRef(), prismContext,
//                repo.owner, RReferenceOwner.CREATE_APPROVER));
//        repo.getModifyApproverRef().addAll(RUtil.safeListReferenceToSet(jaxb.getModifyApproverRef(), prismContext,
//                repo.owner, RReferenceOwner.MODIFY_APPROVER));
    }

    public boolean equals(Metadata m1, Metadata m2) {
        if (m1 == m2) return true;

        if (m1.getCreateApproverRef() != null ? !m1.getCreateApproverRef().equals(m2.getCreateApproverRef()) : m2.getCreateApproverRef() != null)
            return false;
        if (m1.getCreateChannel() != null ? !m1.getCreateChannel().equals(m2.getCreateChannel()) : m2.getCreateChannel() != null)
            return false;
        if (m1.getCreateTimestamp() != null ? !m1.getCreateTimestamp().equals(m2.getCreateTimestamp()) : m2.getCreateTimestamp() != null)
            return false;
        if (m1.getCreatorRef() != null ? !m1.getCreatorRef().equals(m2.getCreatorRef()) : m2.getCreatorRef() != null)
            return false;
        if (m1.getModifierRef() != null ? !m1.getModifierRef().equals(m2.getModifierRef()) : m2.getModifierRef() != null)
            return false;
        if (m1.getModifyApproverRef() != null ? !m1.getModifyApproverRef().equals(m2.getModifyApproverRef()) : m2.getModifyApproverRef() != null)
            return false;
        if (m1.getModifyChannel() != null ? !m1.getModifyChannel().equals(m2.getModifyChannel()) : m2.getModifyChannel() != null)
            return false;
        if (m1.getModifyTimestamp() != null ? !m1.getModifyTimestamp().equals(m2.getModifyTimestamp()) : m2.getModifyTimestamp() != null)
            return false;

        return true;
    }
}