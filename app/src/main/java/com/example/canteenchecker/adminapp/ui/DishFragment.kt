package com.example.canteenchecker.adminapp.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.canteenchecker.adminapp.App
import com.example.canteenchecker.adminapp.R
import com.example.canteenchecker.adminapp.api.AdminApiFactory
import com.example.canteenchecker.adminapp.core.Canteen
import com.example.canteenchecker.adminapp.core.EditDish
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

private const val ARG_DISH = "dish"

class DishFragment : Fragment(R.layout.fragment_dish) {
    private val dishFetchedBroadcastReceiver: BroadcastReceiver = DishFetchedBroadcastReceiver()

    private lateinit var edtDish: EditText
    private lateinit var edtPrice: EditText
    private lateinit var btnUpdateDish: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireContext().registerReceiver(
            dishFetchedBroadcastReceiver,
            IntentFilter("com.example.canteenchecker.adminapp.ui.MainActivity.DishFetched")
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        requireContext().unregisterReceiver(dishFetchedBroadcastReceiver)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.run {
            edtDish = findViewById(R.id.edtDish)
            edtPrice = findViewById(R.id.edtPrice)
            btnUpdateDish = findViewById(R.id.btnUpdateDish)

            arguments?.let {
                it.getSerializable(ARG_DISH, EditDish::class.java)?.let { dish ->
                    setValues(dish)
                }
            }
            btnUpdateDish.setOnClickListener { update() }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(ARG_DISH, EditDish(edtDish.text.toString(), edtPrice.text.toString().toDouble()))
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        arguments?.let {
            it.getSerializable(ARG_DISH, EditDish::class.java)?.let { dish ->
                setValues(dish)
            }
        }
    }

    private fun update() = lifecycleScope.launch{
        val token = (requireActivity().application as App).authenticationToken

        val priceWithCurrency = edtPrice.text.toString()

        val priceWithoutCurrency = NumberFormat.getCurrencyInstance().currency.let {
            priceWithCurrency.replace(
                it?.symbol ?: "EUR", "")
        }

        val price = priceWithoutCurrency.toDouble()
        val name = edtDish.text.toString()

        AdminApiFactory.createAdminAPi().updateDish(token, EditDish(name, price))
            .onFailure {
                Toast.makeText(requireContext(), "Verbindung zum Server fehlgeschlagen", Toast.LENGTH_LONG).show()
            }
    }

    private fun setValues(dish: EditDish) {
        edtDish.setText(dish.name)
        edtPrice.setText(NumberFormat.getCurrencyInstance().format(dish.price))
    }

    inner class DishFetchedBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.getSerializableExtra(ARG_DISH, EditDish::class.java)?.let {
                setValues(it)
            }
        }
    }

    companion object {
        @JvmStatic
        fun dishUpdatedIntent(name: String, price: Double) =
            Intent().also { intent ->
                intent.action =
                    "com.example.canteenchecker.adminapp.ui.MainActivity.DishFetched"
                intent.putExtra(ARG_DISH, EditDish(name, price))
            }
        @JvmStatic
        fun newInstance(dish: EditDish) =
            DishFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_DISH, dish)
                }
            }
    }
}