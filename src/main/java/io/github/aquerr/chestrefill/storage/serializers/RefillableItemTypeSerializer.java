package io.github.aquerr.chestrefill.storage.serializers;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import io.github.aquerr.chestrefill.entities.RefillableItem;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.persistence.DataTranslators;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.Tuple;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class has been mostly copied from Nuclues.
 *
 * <p>See https://github.com/NucleusPowered/Nucleus/blob/sponge-api/7/src/main/java/io/github/nucleuspowered/nucleus/configurate/typeserialisers/NucleusItemStackSnapshotSerialiser.java</p>
 *
 * <p>This class, as such, is copyrighted (c) by NucleusPowered team and Nucleus contributors.</p>
 */
public class RefillableItemTypeSerializer implements TypeSerializer<RefillableItem>
{
//    private static final TypeToken<ItemStack> ITEM_STACK_TYPE_TOKEN = TypeToken.of(ItemStack.class);

    @Nullable
    @Override
    public RefillableItem deserialize(@NonNull TypeToken<?> type, @NonNull ConfigurationNode value) throws ObjectMappingException
    {
        final float chance = value.getNode("change").getFloat(1f);
        final int slot = value.getNode("slot").getInt();

        final ConfigurationNode itemNode = value.getNode("item");

        boolean emptyEnchant = false;
        ConfigurationNode ench = itemNode.getNode("UnsafeData", "ench");
        if (!ench.isVirtual())
        {
            List<? extends ConfigurationNode> enchantments = ench.getChildrenList();
            if (enchantments.isEmpty())
            {
                // Remove empty enchantment list.
                itemNode.getNode("UnsafeData").removeChild("ench");
            }
            else
            {
                enchantments.forEach(x -> {
                    try
                    {
                        short id = Short.parseShort(x.getNode("id").getString());
                        short lvl = Short.parseShort(x.getNode("lvl").getString());

                        x.getNode("id").setValue(id);
                        x.getNode("lvl").setValue(lvl);
                    }
                    catch (NumberFormatException e)
                    {
                        x.setValue(null);
                    }
                });
            }
        }

        ConfigurationNode data = itemNode.getNode("Data");
        if (!data.isVirtual() && data.hasListChildren())
        {
            List<? extends ConfigurationNode> n = data.getChildrenList().stream()
                    .filter(x ->
                            !x.getNode("DataClass").getString("").endsWith("SpongeEnchantmentData")
                                    || (!x.getNode("ManipulatorData", "ItemEnchantments").isVirtual() && x.getNode("ManipulatorData", "ItemEnchantments").hasListChildren()))
                    .collect(Collectors.toList());
            emptyEnchant = n.size() != data.getChildrenList().size();

            if (emptyEnchant)
            {
                if (n.isEmpty())
                {
                    itemNode.removeChild("Data");
                }
                else
                {
                    itemNode.getNode("Data").setValue(n);
                }
            }
        }

        DataContainer dataContainer = DataTranslators.CONFIGURATION_NODE.translate(itemNode);
        Set<DataQuery> ldq = dataContainer.getKeys(true);

        for (DataQuery dataQuery : ldq)
        {
            String el = dataQuery.asString(".");
            if (el.contains("$Array$"))
            {
                try
                {
                    Tuple<DataQuery, Object> r = TypeHelper.getArray(dataQuery, dataContainer);
                    dataContainer.set(r.getFirst(), r.getSecond());
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                dataContainer.remove(dataQuery);
            }
        }

        ItemStack snapshot;
        try
        {
            snapshot = ItemStack.builder().fromContainer(dataContainer).build();
        }
        catch (Exception e)
        {
            throw new ObjectMappingException("Could not create Item Stack from data container.");
        }

        // Validate the item.
        if (snapshot.isEmpty() || snapshot.getType() == ItemTypes.NONE)
        {
            // don't bother
            throw new ObjectMappingException("Could not deserialize item. Item is empty.");
        }

        if (emptyEnchant)
        {
            snapshot.offer(Keys.ITEM_ENCHANTMENTS, Lists.newArrayList());
            return new RefillableItem(snapshot, slot, chance);
        }

        if (snapshot.get(Keys.ITEM_ENCHANTMENTS).isPresent())
        {
            // Reset the data.
            snapshot.offer(Keys.ITEM_ENCHANTMENTS, snapshot.get(Keys.ITEM_ENCHANTMENTS).get());
            return new RefillableItem(snapshot, slot, chance);
        }

        return new RefillableItem(snapshot, slot, chance);

//        final float chance = value.getNode("change").getFloat(1f);
//        final int slot = value.getNode("slot").getInt();
//        final ItemStack item = value.getNode("item").getValue(TypeToken.of(ItemStack.class));
//        return new RefillableItem(item, slot, chance);
    }

    @Override
    public void serialize(@NonNull TypeToken<?> type, @Nullable RefillableItem obj, @NonNull ConfigurationNode value) throws ObjectMappingException
    {
        if (obj == null)
            return;

//        value.getNode("chance").setValue(obj.getChance());
//        value.getNode("slot").setValue(obj.getSlot());
//        value.getNode("item").setValue(TypeToken.of(ItemStack.class), obj.getItem());

        final ItemStack itemStack = obj.getItem();
        DataView view;
        try
        {
            view = itemStack.toContainer();
        }
        catch (NullPointerException e)
        {
            System.out.println("BOOM!");
            return;
        }

        final Map<DataQuery, Object> dataQueryObjectMap = view.getValues(true);
        for (final Map.Entry<DataQuery, Object> entry : dataQueryObjectMap.entrySet())
        {
            if (entry.getValue().getClass().isArray())
            {
                if (entry.getValue().getClass().getComponentType().isPrimitive())
                    {
                    DataQuery old = entry.getKey();
                    Tuple<DataQuery, List<?>> dqo = TypeHelper.getList(old, entry.getValue());
                    view.remove(old);
                    view.set(dqo.getFirst(), dqo.getSecond());
                }
                else
                {
                    view.set(entry.getKey(), Lists.newArrayList((Object[]) entry.getValue()));
                }
            }
        }

        value.getNode("chance").setValue(obj.getChance());
        value.getNode("slot").setValue(obj.getSlot());
        final ConfigurationNode itemNode = value.getNode("item");
//        if (itemNode.getOptions().acceptsType(Short.class) && itemNode.getOptions().acceptsType(Byte.class))
            itemNode.setValue(DataTranslators.CONFIGURATION_NODE.translate(view));
//        else
//        {
//            ConfigurationOptions options = itemNode.getOptions().setAcceptedTypes(ImmutableSet.of(Map.class, List.class, Double.class, Float.class, Long.class, Integer.class, Boolean.class, String.class,
//                    Short.class, Byte.class, Number.class));
//
//            final ConfigurationNode fixedNode = SimpleCommentedConfigurationNode.root(options).setValue(DataTranslators.CONFIGURATION_NODE.translate(view));
//            itemNode.setValue(fixedNode);
//        }
    }
}
