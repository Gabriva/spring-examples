package hello.dao.impl;

import hello.container.FieldHolder;
import hello.container.QueryParams;
import hello.dao.BaseDao;
import hello.util.EntityFieldUtils;
import hello.util.ReflectionUtils;
import org.hibernate.Session;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import java.io.Serializable;
import java.util.*;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public abstract class AbstractBaseDao<T, ID extends Serializable> implements BaseDao<T, ID> {

    protected abstract Class<T> getPersistentClass();

    @PersistenceContext
    private EntityManager em;

    protected Session getSession() {
        return em.unwrap(Session.class);
    }

    protected CriteriaBuilder getCriteriaBuilder() {
        return getSession().getCriteriaBuilder();
    }

    protected CriteriaQuery<T> getCriteriaQuery() {
        CriteriaBuilder criteriaBuilder = getCriteriaBuilder();
        return criteriaBuilder.createQuery(getPersistentClass());
    }

    protected Root<T> getRoot(CriteriaQuery<T> criteriaQuery) {
        return criteriaQuery.from(getPersistentClass());
    }

    protected CriteriaQuery<T> getCriteriaQueryWithRoot() {
        CriteriaQuery<T> criteriaQuery = getCriteriaQuery();
        Root<T> root = getRoot(criteriaQuery);
        return criteriaQuery.select(root);
    }

    @Override
    public List<T> getAll() {
        return getSession()
                .createQuery(getCriteriaQueryWithRoot())
                .getResultList();
    }

    @Override
    public Optional<T> getById(ID id) {
        T entity = getSession().get(getPersistentClass(), id);
        return Optional.ofNullable(entity);
    }

    @Override
    public void persist(T entity) {
        getSession().persist(entity);
    }

    @Override
    public void saveOrUpdate(T entity) {
        getSession().saveOrUpdate(entity);
    }

    @Override
    public void delete(T entity) {
        getSession().delete(entity);
    }

    @Override
    public List<T> getByProps(Map<String, List<?>> props) {
        CriteriaQuery<T> criteria = getCriteriaByProps(props);

        return getSession().createQuery(criteria).getResultList();
    }

    @Override
    public List<T> getByProp(String fieldName, Object fieldValue) {
        Map<String, List<?>> mapProps = new HashMap<>();
        mapProps.put(fieldName, singletonList(fieldValue));
        return getByProps(mapProps);
    }

    protected CriteriaQuery<T> getCriteriaByProps(Map<String, List<?>> props) {
        Objects.requireNonNull(props, "Param 'props' cannot be null, sorry");

        CriteriaQuery<T> criteria = getCriteriaQuery();
        Root<T> root = getRoot(criteria);
        criteria.select(root);

        List<Predicate> predicates = props.entrySet().stream()
                .map(e -> getPredicateInByFieldNameAndValues(root, e.getKey(), e.getValue()))
                .collect(toList());

        criteria.where(getCriteriaBuilder().and(predicates.toArray(new Predicate[predicates.size()])));
        return criteria;
    }

    protected Predicate getPredicateInByFieldNameAndValues(Root<T> root, String fieldName, List<?> values) {
        if (values.isEmpty()) return null;

        if (EntityFieldUtils.isRelationField(fieldName)) {
            return getPredicateInRelationField(root, fieldName, values);
        }

        Class<?> fieldType = ReflectionUtils.getFieldType(getPersistentClass(), fieldName);
        return getPredicateInByValuesWithCast(root, fieldName, values, fieldType);
    }

    private Predicate getPredicateInRelationField(Root<T> root, String fieldName, List<?> values) {
        String relationFieldAlias = EntityFieldUtils.getRelationFieldAlias(fieldName);
        String relationFieldName = EntityFieldUtils.getRelationFieldName(fieldName);

        Class<?> relationFieldClass = ReflectionUtils.getFieldType(getPersistentClass(), relationFieldAlias);
        Class<?> relationFieldType = ReflectionUtils.getFieldType(relationFieldClass, relationFieldName);

        Join<T, ?> join = root.join(relationFieldAlias);

        return getPredicateInByValuesByJoinWithCast(join, relationFieldName, values, relationFieldType);
    }

    private Predicate getPredicateInByValuesByJoinWithCast(Join<T, ?> join, String fieldName, List<?> values, Class<?> fieldType) {
        Set<Object> castedValues = getCastedValues(values, fieldType);
        Path<Object> expression = join.get(fieldName);
        return getPredicateIn(castedValues, expression);
    }

    private Predicate getPredicateInByValuesWithCast(Root<T> root, String fieldName, List<?> values, Class<?> fieldType) {
        Set<Object> castedValues = getCastedValues(values, fieldType);
        Path<Object> expression = root.get(fieldName);
        return getPredicateIn(castedValues, expression);
    }

    private Predicate getPredicateIn(Set<Object> castedValues, Path<Object> expression) {
        return expression.in(castedValues);
    }

    private Set<Object> getCastedValues(List<?> values, Class<?> fieldType) {
        return values.stream()
                .map(v -> ReflectionUtils.castFieldValueByType(fieldType, v))
                .collect(toSet());
    }

    @Override
    public List<T> getByFields(Collection<FieldHolder> fieldHolders) {
        //TODO: to do...
        return null;
    }

    @Override
    public List<T> universalQuery(Map<String, List<?>> fields, QueryParams queryParams) {
        //TODO: to do...
        return null;
    }
}