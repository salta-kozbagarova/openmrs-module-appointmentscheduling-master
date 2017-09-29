package org.openmrs.module.appointmentscheduling.api.db.hibernate;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.openmrs.module.appointmentscheduling.PaymentType;
import org.openmrs.module.appointmentscheduling.api.db.PaymentTypeDAO;

public class HibernatePaymentTypeDAO extends HibernateSingleClassDAO implements PaymentTypeDAO {
	
	public HibernatePaymentTypeDAO() {
		super(PaymentType.class);
	}
	
	public List<PaymentType> getPaymentTypes(String fuzzySearchPhrase, boolean includeRetired) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(mappedClass);
		criteria.add(Restrictions.ilike("name", fuzzySearchPhrase, MatchMode.ANYWHERE));
		if (!includeRetired)
			criteria.add(Restrictions.eq("retired", includeRetired));
		criteria.addOrder(Order.asc("name"));
		return criteria.list();
	}
	
	@Override
	public boolean verifyDuplicatedPaymentTypeName(PaymentType paymentType) {
		Criteria criteria = sessionFactory.getCurrentSession().createCriteria(mappedClass);
		criteria.add(Restrictions.eq("name", paymentType.getName()).ignoreCase());
		criteria.add(Restrictions.eq("retired", false));
		criteria.add(Restrictions.not(Restrictions.eq("uuid", paymentType.getUuid())));
		
		return !criteria.list().isEmpty();
	}

}
