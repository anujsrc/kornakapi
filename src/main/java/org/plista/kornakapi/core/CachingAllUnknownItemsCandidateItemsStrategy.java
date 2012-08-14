package org.plista.kornakapi.core;

import org.apache.mahout.cf.taste.common.Refreshable;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.common.LongPrimitiveIterator;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.recommender.CandidateItemsStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class CachingAllUnknownItemsCandidateItemsStrategy implements CandidateItemsStrategy {

  private DataModel dataModel;
  private FastIDSet allItemIDs;

  private static final Logger log = LoggerFactory.getLogger(CachingAllUnknownItemsCandidateItemsStrategy.class);

  public CachingAllUnknownItemsCandidateItemsStrategy(DataModel dataModel) throws TasteException {
    this.dataModel = dataModel;
    allItemIDs = loadAllItemIDs(dataModel);
  }

  @Override
  public FastIDSet getCandidateItems(long userID, PreferenceArray preferencesFromUser, DataModel dataModel)
      throws TasteException {
    FastIDSet possibleItemIDs = allItemIDs.clone();
    possibleItemIDs.removeAll(preferencesFromUser.getIDs());
    return possibleItemIDs;
  }

  private FastIDSet loadAllItemIDs(DataModel dataModel) throws TasteException {

    int numItems = dataModel.getNumItems();

    log.info("Loading {} itemIDs into memory", numItems);

    FastIDSet collectedItemIDs = new FastIDSet(dataModel.getNumItems());
    LongPrimitiveIterator allItemIDsIterator = dataModel.getItemIDs();
    while (allItemIDsIterator.hasNext()) {
      collectedItemIDs.add(allItemIDsIterator.next());
    }
    return collectedItemIDs;
  }

  @Override
  public void refresh(Collection<Refreshable> alreadyRefreshed) {
    try {
      allItemIDs = loadAllItemIDs(dataModel);
    } catch (TasteException e) {
      throw new RuntimeException("Unable to reload itemIDs from DataModel", e);
    }
  }
}