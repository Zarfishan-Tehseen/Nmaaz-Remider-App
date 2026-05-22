package com.example.nmaazreminder.ui.fragments.setting

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nmaazreminder.databinding.FragmentMadhabSelectorBinding
import com.example.nmaazreminder.databinding.ItemMadhabRowBinding

class MadhabSelectorFragment : Fragment() {
    private var _binding: FragmentMadhabSelectorBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMadhabSelectorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        val madhabList = listOf(
            MadhabOption(
                id = 1,
                title = "Hanafi",
                description = "Asr: shadow length × 2 of object",
                regions = "Common in South & Central Asia"
            ),
            MadhabOption(
                id = 2,
                title = "Shafiʻi · Maliki · Hanbali",
                description = "Asr: shadow length × 1 of object",
                regions = "Common in Arab world, SE Asia"
            )
        )

        // 3. Configure RecyclerView with a LayoutManager
        binding.rvMadhabMethods.layoutManager = LinearLayoutManager(requireContext())

        // 4. Initialize and set the Adapter (Defaulting to Shafi'i index 1 matching your UI mockup)
        val madhabAdapter = MadhabAdapter(madhabList, selectedPosition = 1) { selectedMadhab ->
            // TODO: Save the selected option to SharedPreferences or DataStore here
            Toast.makeText(requireContext(), "Selected: ${selectedMadhab.title}", Toast.LENGTH_SHORT).show()
        }

        binding.rvMadhabMethods.adapter = madhabAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Clear memory reference to avoid memory leaks
    }
}
data class MadhabOption(
    val id: Int,
    val title: String,
    val description: String,
    val regions: String
)

class MadhabAdapter(
    private val items: List<MadhabOption>,
    private var selectedPosition: Int,
    private val onMadhabSelected: (MadhabOption) -> Unit
) : RecyclerView.Adapter<MadhabAdapter.MadhabViewHolder>() {

    inner class MadhabViewHolder(val binding: ItemMadhabRowBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MadhabViewHolder {
        val binding = ItemMadhabRowBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MadhabViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MadhabViewHolder, position: Int) {
        val currentItem = items[position]

        holder.binding.apply {
            tvMadhabTitle.text = currentItem.title
            tvMadhabDescription.text = currentItem.description
            tvMadhabRegions.text = currentItem.regions

            radioButton.isChecked = (position == selectedPosition)

            dividerLine.visibility = if (position == items.size - 1) View.GONE else View.VISIBLE

            rootItemLayout.setOnClickListener {
                if (selectedPosition != holder.adapterPosition) {
                    val oldPosition = selectedPosition
                    selectedPosition = holder.adapterPosition

                    notifyItemChanged(oldPosition)
                    notifyItemChanged(selectedPosition)

                    onMadhabSelected(currentItem)
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size
}