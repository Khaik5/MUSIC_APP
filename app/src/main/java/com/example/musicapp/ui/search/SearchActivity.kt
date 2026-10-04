package com.example.musicapp.ui.search

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.musicapp.data.model.Music
import com.example.musicapp.databinding.ActivitySearchBinding
import com.example.musicapp.databinding.ItemSongBinding
import com.example.musicapp.ui.detail.DetailActivity
import com.example.musicapp.utils.MusicConstants
import com.example.musicapp.utils.applySystemBarsInsets
import kotlinx.coroutines.launch

class SearchActivity : AppCompatActivity() {
    private val binding by lazy { ActivitySearchBinding.inflate(layoutInflater) }
    private val viewModel: SearchViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        binding.root.applySystemBarsInsets()

        binding.etSearch.doAfterTextChanged {
            viewModel.onEvent(SearchEvent.QueryChanged(it?.toString().orEmpty()))
        }
        binding.tvCancel.setOnClickListener {
            viewModel.onEvent(SearchEvent.CancelClicked)
        }

        observeState()
        observeEffect()
        showKeyboard()
    }

    private fun observeState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { render(it) }
            }
        }
    }

    private fun observeEffect() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.effect.collect { effect ->
                    when (effect) {
                        is SearchEffect.NavigateToDetail -> {
                            startActivity(Intent(this@SearchActivity, DetailActivity::class.java).apply {
                                putExtra(MusicConstants.EXTRA_SONG_ID, effect.songId)
                                putExtra(MusicConstants.EXTRA_AUTO_PLAY, true)
                            })
                        }
                        SearchEffect.Close -> finish()
                    }
                }
            }
        }
    }

    private fun render(state: SearchState) {
        binding.resultContainer.removeAllViews()
        state.results.forEach { song -> addSong(song) }
    }

    private fun addSong(song: Music) {
        val item = ItemSongBinding.inflate(
            LayoutInflater.from(this), binding.resultContainer, false
        )
        item.ivCover.setImageResource(song.imageRes)
        item.tvTitle.text = song.title
        item.tvArtist.text = song.artist
        item.tvMeta.text = song.meta
        item.ivLiveDot.visibility = if (song.id == "90s_album" || song.id == "cancer") {
            android.view.View.VISIBLE
        } else android.view.View.GONE
        item.root.setOnClickListener {
            viewModel.onEvent(SearchEvent.SongClicked(song.id))
        }
        item.btnMore.setOnClickListener {
            viewModel.onEvent(SearchEvent.SongClicked(song.id))
        }
        binding.resultContainer.addView(item.root)
    }

    private fun showKeyboard() {
        binding.etSearch.requestFocus()
        binding.etSearch.postDelayed({
            getSystemService<InputMethodManager>()?.showSoftInput(
                binding.etSearch, InputMethodManager.SHOW_IMPLICIT
            )
        }, 200)
    }
}
