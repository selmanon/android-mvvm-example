package de.rheinfabrik.mvvm_example.activities;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import butterknife.InjectView;
import butterknife.Views;
import de.rheinfabrik.mvvm_example.R;
import de.rheinfabrik.mvvm_example.adapter.SearchResultsAdapter;
import de.rheinfabrik.mvvm_example.ui.views.SearchToolbar;
import de.rheinfabrik.mvvm_example.utils.KeyboardHandler;
import de.rheinfabrik.mvvm_example.viewmodels.SearchViewModel;
import rx.android.schedulers.AndroidSchedulers;

import static com.jakewharton.rxbinding.widget.RxTextView.textChanges;

/**
 * Activity which is responsible for searching and displaying the results.
 */
public class SearchActivity extends RxAppCompatActivity {

    // Members

    private SearchViewModel mSearchViewModel;
    private MenuItem mSearchItem;

    @InjectView(R.id.search_toolbar)
    protected SearchToolbar mToolbar;

    @InjectView(R.id.search_results_recycler_view)
    protected RecyclerView mRecyclerView;

    // Activity lifecycle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // View
        setContentView(R.layout.activity_search);
        Views.inject(this);
        setSupportActionBar(mToolbar);

        // Recycler
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        // View model
        mSearchViewModel = new SearchViewModel();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Bind show/hide search and show correct icon
        mSearchViewModel.showSearch()
                .map(showSearch -> showSearch ? R.mipmap.ic_action_close : R.mipmap.ic_action_search)
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(icon -> {

                    // Do NOT replace with lambda method reference as mSearchAction might be null.
                    if (mSearchItem != null) {
                        mSearchItem.setIcon(icon);
                    }
                });

        // Bind show/hide search UI in toolbar
        mSearchViewModel.showSearch()
                .observeOn(AndroidSchedulers.mainThread())
                .compose(bindToLifecycle())
                .subscribe(mToolbar::setSearchUIVisible);

        // Bind show/hide keyboard
        mSearchViewModel.showSearch()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(showKeyboard -> {
                    if (showKeyboard) {
                        KeyboardHandler.showKeyboard(this, mToolbar.getSearchEditTextView());
                    } else {
                        KeyboardHandler.hideKeyboard(this, mToolbar.getSearchEditTextView());
                    }
                });

        // Bind text input and start search
        textChanges(mToolbar.getSearchEditTextView())
                .map(CharSequence::toString)
                .compose(bindToLifecycle())
                .subscribe(mSearchViewModel.searchCommand::call);

        // Bind search results
        mSearchViewModel.searchResults()
                .map(results -> new SearchResultsAdapter(this, results))
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mRecyclerView::setAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        mSearchItem = menu.findItem(R.id.action_search);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item == mSearchItem) {
            mSearchViewModel.toggleSearchVisibilityCommand.call(null);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
