package com.imooc.service.search;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.imooc.entity.House;
import com.imooc.entity.HouseDetail;
import com.imooc.entity.HouseTag;
import com.imooc.repository.HouseDetailRepository;
import com.imooc.repository.HouseRepository;
import com.imooc.repository.HouseTagRepository;
import com.imooc.service.ServiceMultiResult;
import com.imooc.web.form.RentSearch;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.BulkByScrollResponse;
import org.elasticsearch.index.reindex.DeleteByQueryAction;
import org.elasticsearch.index.reindex.DeleteByQueryRequestBuilder;
import org.elasticsearch.rest.RestStatus;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;


@Service
public class SearchServiceImpl implements ISearchService {

    private static final Logger log = LoggerFactory.getLogger(ISearchService.class);

    private static final String INDEX_NAME="xunwu";

    private static final String INDEX_TYPE="house";

    @Autowired
    HouseRepository houseRepository;
    @Autowired
    ModelMapper modelMapper;
    @Autowired
    TransportClient esClient;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    HouseDetailRepository detailRepository;
    @Autowired
    HouseTagRepository tagRepository;

    @Override
    public boolean index(Long houseId) {
       House house = houseRepository.findOne(houseId);
       if (null == house){
           log.error("Index house {} does not exist!",houseId);
           return false;
       }

       HouseIndexTemplate indexTemplate =modelMapper.map(house,HouseIndexTemplate.class);

       HouseDetail detail = detailRepository.findAllByHouseId(houseId);
       if (null ==detail){
       }
       modelMapper.map(detail,HouseIndexTemplate.class);

       List<HouseTag> tagList = tagRepository.findAllByHouseId(houseId);
       if (!CollectionUtils.isEmpty(tagList)){
           List<String> tagStrList = new ArrayList<>();
           tagList.forEach(tag->{tagStrList.add(tag.getName());});
           indexTemplate.setTags(tagStrList);
       }

        SearchRequestBuilder requestBuilder= this.esClient.prepareSearch(INDEX_NAME).setTypes(INDEX_TYPE).setQuery(QueryBuilders.termQuery(HouseIndexKey.HOUSE_ID,houseId));

       log.debug(requestBuilder.toString());

        boolean success;
        SearchResponse searchResponse = requestBuilder.get();
        long totalHit = searchResponse.getHits().getTotalHits();
        if (totalHit == 0){
            success= create(indexTemplate);
        }else if (totalHit == 1){
            String esId =searchResponse.getHits().getAt(0).getId();
            success=update(esId,indexTemplate);
        }else {
            success= deleteAndCreate(totalHit,indexTemplate);
        }

        return success;
    }

    private boolean create(HouseIndexTemplate indexTemplate){
        try {
            IndexResponse response = this.esClient.prepareIndex(INDEX_NAME,INDEX_TYPE)
                    .setSource(objectMapper.writeValueAsBytes(indexTemplate),
                    XContentType.JSON).get();

            log.debug("Create index with house:"+indexTemplate.getHouseId()); //todo ??
            if (response.status() == RestStatus.CREATED){
                return true;
            }else {
                return false;
            }
        } catch (JsonProcessingException e) {
            log.error("Error to index house" + indexTemplate.getHouseId(),e);
            return false;
        }
    }

    public boolean update(String esId,HouseIndexTemplate indexTemplate){
        try {
            UpdateResponse response = this.esClient.prepareUpdate(INDEX_NAME,INDEX_TYPE,esId)
                    .setDoc(objectMapper.writeValueAsBytes(indexTemplate),
                            XContentType.JSON).get();

            log.debug("update index with house:"+indexTemplate.getHouseId()); //todo ??
            if (response.status() == RestStatus.OK){
                return true;
            }else {
                return false;
            }
        } catch (JsonProcessingException e) {
            log.error("Error to index house" + indexTemplate.getHouseId(),e);
            return false;
        }
    }

    public boolean deleteAndCreate(long totalHit,HouseIndexTemplate houseIndexTemplate){
           DeleteByQueryRequestBuilder builder = DeleteByQueryAction.INSTANCE
                .newRequestBuilder(esClient)
                .filter(QueryBuilders.termQuery(HouseIndexKey.HOUSE_ID,houseIndexTemplate.getHouseId()))
                .source(INDEX_NAME);
           log.debug("Delete by query for house:"+builder);
           BulkByScrollResponse response = builder.get();
           long delete = response.getDeleted();
           if (delete != totalHit){
               log.warn("Need delete {},but {} was delete",totalHit,delete);
               return false;
           }else {
               return create(houseIndexTemplate);
           }
    }


    @Override
    public void remove(Long houseId) {
        DeleteByQueryRequestBuilder builder = DeleteByQueryAction.INSTANCE
                .newRequestBuilder(esClient)
                .filter(QueryBuilders.termQuery(HouseIndexKey.HOUSE_ID,houseId))
                .source(INDEX_NAME);
        log.debug("Delete by query for house:" + builder);
        BulkByScrollResponse response = builder.get();
        long delete = response.getDeleted();
        log.debug("Delete total"+delete);
    }

    @Override
    public ServiceMultiResult<Long> query(RentSearch rentSearch) {
        return null;
    }

















}
