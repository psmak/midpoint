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
 * Portions Copyrighted 2013 [name of copyright owner]
 */

package com.evolveum.midpoint.repo.sql.query2;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.query.ObjectFilter;
import com.evolveum.midpoint.repo.sql.query.QueryException;
import com.evolveum.midpoint.repo.sql.query2.restriction.Restriction;
import com.evolveum.midpoint.util.ClassPathUtil;
import com.evolveum.midpoint.util.exception.SystemException;
import com.evolveum.midpoint.util.logging.LoggingUtils;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.xml.ns._public.common.common_2a.ObjectType;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.reflect.ConstructorUtils;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author lazyman
 */
public class QueryInterpreter {

    private static final Trace LOGGER = TraceManager.getTrace(QueryInterpreter.class);
    private static final Set<Restriction> AVAILABLE_RESTRICTIONS;

    static {
        Set<Restriction> restrictions = new HashSet<Restriction>();

        String packageName = Restriction.class.getPackage().getName();
        Set<Class> classes = ClassPathUtil.listClasses(packageName);
        LOGGER.debug("Found {} classes in package {}.", new Object[]{classes.size(), packageName});
        for (Class<Restriction> clazz : classes) {
            if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
                //we don't need interfaces and abstract classes
                continue;
            }

            if (!clazz.isAssignableFrom(Restriction.class)) {
                //we don't need classes that don't inherit from Restriction
                continue;
            }

            try {
                Restriction restriction = (Restriction) ConstructorUtils.invokeConstructor(clazz, null);
                restrictions.add(restriction);

                LOGGER.debug("Added {} instance to available restrictions.", new Object[]{restriction});
            } catch (Exception ex) {
                LoggingUtils.logException(LOGGER, "Error occurred during query interpreter initialization", ex);
                if (ex instanceof SystemException) {
                    throw (SystemException) ex;
                }
                throw new SystemException(ex.getMessage(), ex);
            }
        }

        AVAILABLE_RESTRICTIONS = Collections.unmodifiableSet(restrictions);
    }

    public QueryInterpreter(PrismContext context) {
    }

    public Criteria interpret(ObjectFilter filter, Class<? extends ObjectType> type,  Session session)
            throws QueryException {
        Validate.notNull(filter, "Element filter must not be null.");

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Interpreting query '{}', query on trace level.", new Object[]{filter.getClass()});
            LOGGER.trace("Query filter:\n{}", new Object[]{filter.dump()});
        }

        try {
            Restriction restriction = findAndCreateRestriction(filter);
            QueryContext context = new QueryContext(type, session);
            Criterion criterion = restriction.interpret();

            Criteria criteria = context.getMainCriteria();
            criteria.add(criterion);

            return criteria;
        } catch (QueryException ex) {
            throw ex;
        } catch (Exception ex) {
            LOGGER.trace(ex.getMessage(), ex);
            throw new QueryException(ex.getMessage(), ex);
        }
    }

    public <T extends ObjectFilter> Restriction findAndCreateRestriction(T filter) throws QueryException {
        for (Restriction restriction : AVAILABLE_RESTRICTIONS) {
            if (restriction.canHandle(filter)) {
                return restriction;
            }
        }

        LOGGER.error("Couldn't find proper restriction that can handle filter '{}'.", new Object[]{filter.dump()});
        throw new QueryException("Couldn't find proper restriction that can handle '" + filter + "'");
    }
}