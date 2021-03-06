package hello.dao;

import hello.container.FieldHolder;
import hello.container.OrderType;
import hello.container.QueryParams;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface BaseDao<T, ID extends Serializable> {

    public List<T> getAll();

    public Optional<T> getById(ID id);

    public void persist(T entity);

    public void saveOrUpdate(T entity);

    public void delete(T entity);

    /**
     * The advantage of this update method is that it performs multiple updates in a single query. Very fast
     *
     * Example:
     * <pre>{@code
     *       UPDATE `planned_operation` SET position_in_list = CASE position_in_list
     *                  WHEN 10 THEN 1
     *                  WHEN 20 THEN 2
     *               END
     *        WHERE position_in_list IN (10, 20);
     * }</pre>
     *
     * @param fieldName fieldName
     * @param mapOldNewValue mapOldNewValue
     */
    public void updateMultiple(String fieldName, Map<?, ?> mapOldNewValue);

    /**
     * When passed empty map or all not exist fields - return all
     * When passed existing fields by not exist values - return empty list
     *
     * For create query with relation object - pass field in format: "<fieldName>.id" (Ex.: "post.id")
     * and for relation field you can passed only one value
     *
     * @param props props
     * @return list of entities
     */
    public List<T> getByProps(Map<String, List<?>> props);

    /**
     * Like {@link #getByProps} but only one field value
     *
     * @param fieldName fieldName
     * @param fieldValue fieldValue
     * @return list of entities
     */
    public List<T> getByProp(String fieldName, Object fieldValue);

    /**
     * When passed empty fieldHolders - return empty list
     * When passed all not exist fields name - return empty list
     *
     * @param fieldHolders fieldHolders
     * @return list of entities
     */
    public List<T> getByFields(Collection<FieldHolder> fieldHolders);

    /**
     * Universal method for create complex query - useful for REST API
     *
     * @param fields fields
     * @param queryParams queryParams
     * @return list of entities
     */
    public List<T> universalQuery(Map<String, List<?>> fields, QueryParams queryParams);
}
